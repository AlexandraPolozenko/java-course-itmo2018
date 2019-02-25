package ru.ifmo.rain.polozenko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.PatternSyntaxException;


/**
 * @author Alexandra Polozenko
 * @see info.kgeorgiy.java.advanced.implementor.Impler
 * @see info.kgeorgiy.java.advanced.implementor.ImplerException
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 */
public class Implementor implements Impler, JarImpler {


    /**
     * Default constructor
     */
    public Implementor() {

    }

    /**
     * Calls implement or implementJar function if jar-file of this class called from command line
     *
     * @param args parametrs from command line
     */
    public static void main(String args[]) {
        Implementor implementor = new Implementor();

        try {
            if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            }
        } catch (ImplerException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Produces code implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name will be same as full name of the type token with <tt>Impl</tt> suffix
     * added. Generated source code will be placed in the correct subdirectory of the specified
     * <tt>root</tt> directory and will have correct file name. For example, the implementation of the
     * interface {@link java.util.List} will go to <tt>$root/java/util/ListImpl.java</tt>.
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("Argument must be a name of an interface");
        }

        String tab = "    ";
        String className = token.getSimpleName() + "Impl";
        String packageName = null;
        Path filePath;
        Path dirPath;

        try {
            dirPath = root.resolve(getPackagePath(token));
            filePath = dirPath.resolve(className + ".java");
        } catch (NullPointerException | InvalidPathException e) {
            throw new ImplerException(e);
        }

        if (token.getPackage() != null) {
            packageName = token.getPackage().getName();
        }

        try {
            Files.createDirectories(dirPath);
        } catch (IOException | InvalidPathException e) {
            throw new ImplerException(e);
        }

        // try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {

        try (Writer writer = new FilterWriter(Files.newBufferedWriter(filePath)) {
            @Override
            public void write(String str) throws IOException {
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);

                    if (c >= 128) {
                        out.write("\\u" + String.format("%04X", (int) c));
                    } else {
                        out.write(c);
                    }
                }
            }
        }) {


            if (packageName != null) {
                writer.write("package " + packageName + ";\n");
            }

            writer.write("public class " + className + " implements " + token.getCanonicalName() + " {\n");

            for (Method m : token.getMethods()) {
                if (m.isDefault() || Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                writer.write(tab + "public " + m.getReturnType().getCanonicalName() + " " + m.getName() + "(");

                Parameter[] param = m.getParameters();
                for (int i = 0; i < param.length; ++i) {
                    if (i != 0) {
                        writer.write(", ");
                    }
                    writer.write(param[i].getType().getCanonicalName() + " " + param[i].getName());
                }

                writer.write(") {\n" + tab + tab + "return " + returnType(m.getReturnType()) + ";\n" + tab + "}\n");
            }

            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name will be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     * Using {@link java.util.jar.Manifest}, {@link java.util.jar.JarOutputStream}, {@link java.nio.file.Paths},
     * {@link javax.tools.JavaCompiler}
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tmpPath = Paths.get(System.getProperty("user.home"));
        tmpPath = tmpPath.resolve(".implementor");

        try {
            Files.createDirectory(tmpPath);
        } catch (IOException e) {
            throw new ImplerException(e);
        }

        implement(token, tmpPath);

        Path dirPath = tmpPath.resolve(getPackagePath(token));
        Path javaPath = dirPath.resolve(token.getSimpleName() + "Impl.java");
        Path classPath = dirPath.resolve(token.getSimpleName() + "Impl.class");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int exitCode = compiler.run(null, null, null,
                javaPath.toString(), "-cp", System.getProperty("java.class.path"));
        if (exitCode != 0) {
            throw new ImplerException("Сompilation error");
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), manifest);
             BufferedInputStream reader = new BufferedInputStream(new FileInputStream(classPath.toString()))) {

            String classZipPath = getPackagePath(token) + "/" + token.getSimpleName() + "Impl.class";
            writer.putNextEntry(new JarEntry(classZipPath));

            byte[] buffer = new byte[1024];
            int r;
            while ((r = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, r);
            }

            writer.closeEntry();
        } catch (IOException e) {
            throw new ImplerException(e);
        } finally {
            try {
                Files.walkFileTree(tmpPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a string representation of the default return type of given class <tt>type</tt>.
     *
     * @param type return type of a class
     * @return string of a default value for given return type
     */
    private static String returnType(Class<?> type) {
        if (type.isPrimitive()) {
            if (type.equals(byte.class)) return "(byte)0";
            if (type.equals(short.class)) return "(short)0";
            if (type.equals(long.class)) return "0L";
            if (type.equals(float.class)) return "0.0f";
            if (type.equals(double.class)) return "0.0d";
            if (type.equals(char.class)) return "'\u0000'";
            if (type.equals(boolean.class)) return "false";
            if (type.equals(void.class)) return "";
            return "0";
        } else if (type.isArray()) {
            return "new " + type.getCanonicalName().replace("[]", "[0]");
        } else return "null";
    }

    /**
     * Returns a string representation of the package's path of given class <tt>type</tt>.
     *
     * @param token given class
     * @return string of a package's path (empty if class is null)
     */
    private String getPackagePath(Class<?> token) {
        if (token != null) {
            return token.getPackage().getName().replace('.', File.separatorChar);
        } else {
            return "";
        }
    }
}
