package test;

import Parser.RParser;

import java.io.File;
import java.io.IOException;

public class SyntaxCheck {
    public static void main(String args[]) throws IOException {
        final File folder = new File("./src/TestCases");
        parseFiles(folder);
    }

    static void parseFiles(final File folder) throws IOException {
        for (final File fileEntry : folder.listFiles()) {
            System.out.println("\nParsing ".concat(fileEntry.getName()));
            new RParser(fileEntry.getAbsolutePath()).parse();
        }
    }
}

