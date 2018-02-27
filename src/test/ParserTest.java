package test;

import Parser.RFileReader;
import Parser.RParser;
import Parser.RScanner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class ParserTest {
    private static final String PATH = "./src/TestCases/";

    public static void main(String args[]) {
//        readFile();
//        scanFile();
        parseAllFiles();
//        parseFile("test025.txt");
    }

    private static void parseFile(String fileName) {
        RParser p = null;
        try {
            p = new RParser(PATH.concat(fileName));
            p.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseAllFiles() {
        try {
            File[] files = new File(PATH).listFiles();
            for (File file : files) {
                if (file.isFile()) {
//                    try {
                        System.out.println("\nParsing ".concat(file.getName()));
                        RParser p = new RParser(PATH.concat(file.getName()));
                        p.parse();
//                    }catch (Exception e){
//                        System.out.println("FAILED:  ".concat(file.getName()));
//                    }
                }
            }
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
            RScanner scanner = new RScanner("./src/Test/test009.txt");
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
