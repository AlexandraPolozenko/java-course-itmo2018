package ru.ifmo.rain.polozenko.implementor;

import java.nio.file.Paths;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

/**
 * @author Alexandra Poloxenko
 * @see ru.ifmo.rain.polozenko.implementor.Implementor
 */
public class Main {
    /**
     * Method used as a main for running Implementor from command line.
     *
     * @param args Arguments from command line
     */
    public static void main(String[] args) {
        Implementor implementor = new Implementor();
        try {
            if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Paths.get("."));
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Usage: java -jar Implementor.jar [<classname> | -jar <classname> <output.jar>]");
        } catch (ClassNotFoundException e) {
            System.err.println("no such class: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("error while implementing class: " + e.getMessage());
        }
    }
}
