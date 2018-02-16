package test;

import Parser.RFileReader;
import Parser.RParser;
import Parser.RScanner;

import java.io.IOException;
import java.util.Scanner;

public class ParserTest {
    public static void main(String args[]) {
//        readFile();
//        scanFile();
        parseFile();
    }

    private static void parseFile() {
        try {
            RParser p = new RParser("./src/TestCases/test009.txt");
            p.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readFile() {
        try {
            RFileReader fr = new RFileReader("./src/Test/test001.txt");
            char c;
            do {
                c = fr.GetSym();
                System.out.print(c);
            } while ((int) c != -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void scanFile() {
        try {
            RScanner scanner = new RScanner("./src/Test/test001.txt");
            Scanner sc = new Scanner(System.in);
            sc.nextLine();
            String input;
            do {
                System.out.println(scanner.getSym());
                input = sc.nextLine();
            } while (!"x".equals(input));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
