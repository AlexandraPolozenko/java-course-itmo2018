package ru.ifmo.rain.polozenko.text;

import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.tools.corba.se.idl.InvalidArgument;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Locale;

public class TextStatistics {
    Locale inputLocale, outputLocale;
    String input, output;
    BreakIterator iterator;


    public static void main(String[] args) {
        if (args == null || args.length != 4) {
            System.err.println("Invalid arguments");
        }
        for (int i = 0; i < 4; i++) {
            if (args[i] == null) {
                System.err.println("Invalid arguments");
            }
        }

        try {
            TextStatistics textStatistics = new TextStatistics(new Locale(args[0]), new Locale(args[1]), args[2], args[3]);

        } catch (Exception e) {
            System.err.println("Unabel to create ");
        }
    }

    public TextStatistics(Locale inputLocale, Locale outputLocale, String input, String output) {
        if (!(outputLocale.equals(Locale.ENGLISH) && outputLocale.equals(Locale.ROOT))) {
            System.err.println("Output local is not supported");
        } else {
            this.inputLocale = inputLocale;
            this.outputLocale = outputLocale;
            this.input = input;
            this.output = output;
            iterator = new BreakIterator() {
                @Override
                public int first() {
                    return 0;
                }

                @Override
                public int last() {
                    return 0;
                }

                @Override
                public int next(int n) {
                    return 0;
                }

                @Override
                public int next() {
                    return 0;
                }

                @Override
                public int previous() {
                    return 0;
                }

                @Override
                public int following(int offset) {
                    return 0;
                }

                @Override
                public int current() {
                    return 0;
                }

                @Override
                public CharacterIterator getText() {
                    return null;
                }

                @Override
                public void setText(CharacterIterator newText) {

                }
            };
        }
    }

    public void analyseText () {
        BreakIterator it1 = iterator.getSentenceInstance(inputLocale);
    }

}
