package Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RFileReader {  // encapsulates streams of characters
    private BufferedReader in;

    public char GetSym() throws IOException {
        int i = in.read();
        if (i == -1) {
            Error();
        }
        return (char) i;
    } // return current and advance to the next character on the input

    private void Error() throws IOException {
//        System.out.println("Reached EOF");
        in.close();
    } // signal an error with current file position


    public RFileReader(String fileName) throws FileNotFoundException { // constructor: open file
        in = new BufferedReader(new FileReader(fileName));
    }
}
