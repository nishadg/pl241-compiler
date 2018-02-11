package Parser;

import java.io.IOException;

public class RScanner { // encapsulates streams of tokens

    private RFileReader fileReader;
    private StringBuilder tokenBuilder;

    int getLineNum() {
        return lineNum;
    }

    private int lineNum = 1;

    private char inputSym; // the current character on the input


    public int getSym() {
        tokenBuilder = new StringBuilder();
        // return current and advance to the next token on the input public
        try {
            while(Token.whitespaces.contains(inputSym)){    // ignore whitespaces
                if(inputSym == '\n') lineNum++;
                inputSym = fileReader.GetSym();
            }

            String sym;
            if (Character.isDigit(inputSym)) {          // token is a number
                handleNumber();
                sym = tokenBuilder.toString();
                number = Integer.parseInt(sym);
                return Token.number;
            } else if (Character.isLetter(inputSym)) {  // token is an identifier or keyword
                handleIdentifier();
                sym = tokenBuilder.toString();
                if (Token.keywords.contains(sym)) {         // keyword
                    return Token.tokenValueMap.get(sym);
                } else {                                    // identifier
                    return Token.ident;
                }
            } else {                                    // token is a symbol
                handleSymbol();
                sym = tokenBuilder.toString();
                if(Token.comments.contains(sym)){
                    skipLine();
                    return getSym();
                }
                if(Token.tokenValueMap.containsKey(sym)) {
                    return Token.tokenValueMap.get(sym);
                }else{
                    Error("Invalid symbol encountered : ".concat(sym) );
                }
            }
        } catch (IOException e) {
            Error(e.getMessage());
        }
        return 0;
    }

    private void skipLine() throws IOException {
        while(inputSym != '\n'){
            inputSym = fileReader.GetSym();
        }
    }

    private void handleSymbol() throws IOException {
        tokenBuilder.append(inputSym);
        if(Token.singleTokens.contains(inputSym)){
            inputSym = fileReader.GetSym();
            return;
        }
        inputSym = fileReader.GetSym();
        if(!(Token.whitespaces.contains(inputSym) || Character.isLetterOrDigit(inputSym))){
            handleSymbol();
        }

    }

    private void handleIdentifier() throws IOException {
        tokenBuilder.append(inputSym);
        inputSym = fileReader.GetSym();
        if (Character.isLetterOrDigit(inputSym)) {
            handleIdentifier();
        }
    }

    private void handleNumber() throws IOException {
        tokenBuilder.append(inputSym);
        inputSym = fileReader.GetSym();
        if (Character.isDigit(inputSym)) {
            handleNumber();
        }
    }

    public int number; // the last number encountered
    public int id; // the last identifier encountered

    /**
     * Signal an error message
     **/
    private void Error(String errorMsg) {
        System.out.println(errorMsg);
    }

    public RScanner(String fileName) throws IOException {
        fileReader = new RFileReader(fileName);
        inputSym = fileReader.GetSym();
    }
}
