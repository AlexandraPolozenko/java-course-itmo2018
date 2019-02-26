package ru.ifmo.rain.polozenko.walk;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Walk {

    public static void main(String[] args) {
        Walk walk = new Walk();

        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
        } else {
            walk.go(args[0], args[1]);
        }
    }

    private void go(String r, String w) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(r), StandardCharsets.UTF_8)) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(w), StandardCharsets.UTF_8)) {
                String s;

                try {
                    while ((s = reader.readLine()) != null && s.length() > 0) {
                        try {
                            writer.write(String.format("%08x", fnv(s)) + " " + s + "\n");
                        } catch (IOException e) {
                            System.err.println("Unable to write in file");
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Unable to read the line");
                }

            } catch (NoSuchFileException e) {
                System.err.println("Output file doesn't exist");
            } catch (InvalidPathException e) {
                System.err.println("Invalid output file path");
            } catch (IOException | SecurityException e) {
                System.err.println("Output file error");
            }
        } catch (NoSuchFileException e) {
            System.err.println("Input file doesn't exist");
        } catch (InvalidPathException e) {
            System.err.println("Invalid input file path");
        } catch (IOException | SecurityException e) {
            System.err.println("Input file error");
        }
    }

    private int fnv(String s) {
        try (FileInputStream cnt = new FileInputStream(s)) {
            int hval = 0x811c9dc5;
            byte[] buf = new byte[4096];
            int n, i;

            n = cnt.read(buf);
            while (n != -1) {
                for (i = 0; i < n; i++) {
                    hval = (hval * 0x01000193) ^ (buf[i] & 0xff);
                }
                n = cnt.read(buf);
            }
            return hval;
        } catch (IOException e) {
            return 0;
        }
    }
}
