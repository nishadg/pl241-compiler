package Parser;

import IR.ScopeManager;
import Model.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RScanner { // encapsulates streams of tokens

    private RFileReader fileReader;
    private StringBuilder tokenBuilder;
    boolean declareMode; // when true, all identifiers will be added to scope without check for existing.

    public String getCurrentToken() {
        return currentToken;
    }

    private String currentToken;


    private HashMap<String, Integer> inbuiltFunctions = new HashMap<>(Map.of(
            "InputNum", 0,
            "OutputNum", 1,
            "OutputNewLine", 2
    ));

    private HashMap<Integer, String> IDToString = new HashMap<>(Map.of(
            0, "InputNum",
            1, "OutputNum",
            2, "OutputNewLine"
    ));

    public int getLineNum() {
        return lineNum;
    }

    private int lineNum = 1;

    private char inputSym; // the current character on the input


    public int getSym() {
        tokenBuilder = new StringBuilder();
        // return current and advance to the next token on the input public
        try {
            while (Token.whitespaces.contains(inputSym)) {    // ignore whitespaces
                if (inputSym == '\n') lineNum++;
                inputSym = fileReader.GetSym();
            }

            if (Character.isDigit(inputSym)) {          // token is a number
                handleNumber();
                currentToken = tokenBuilder.toString();
                number = Integer.parseInt(currentToken);
                return Token.number;
            } else if (Character.isLetter(inputSym)) {  // token is an identifier or keyword
                handleIdentifier();
                currentToken = tokenBuilder.toString();
                if (Token.keywords.contains(currentToken)) {         // keyword
                    return Token.tokenValueMap.get(currentToken);
                } else {                                    // identifier
                    if (Token.inbuiltFunctions.contains(currentToken)) {
                        id = inbuiltFunctions.get(currentToken);
                    } else {
                        Variable v = null;
                        if(!declareMode){
                            v = ScopeManager.INSTANCE.findTokenInScope(currentToken);
                        }
                        if (v != null) {
                            id = v.getId();
                        } else {
                            id = idCounter++;
                            ScopeManager.INSTANCE.createVar(id, currentToken);
                        }
                    }
                    return Token.ident;
                }
            } else {                                    // token is a symbol
                handleSymbol();
                currentToken = tokenBuilder.toString();
                if (Token.comments.contains(currentToken)) {
                    skipLine();
                    return getSym();
                }
                if (Token.tokenValueMap.containsKey(currentToken)) {
                    return Token.tokenValueMap.get(currentToken);
                } else {
                    Error("Invalid symbol encountered : ".concat(currentToken));
                }
            }
        } catch (IOException e) {
            Error(e.getMessage());
        }
        return 0;
    }

    private void skipLine() throws IOException {
        while (inputSym != '\n') {
            inputSym = fileReader.GetSym();
        }
    }

    private void handleSymbol() throws IOException {
        tokenBuilder.append(inputSym);
        if (Token.singleTokens.contains(inputSym)) {
            inputSym = fileReader.GetSym();
            return;
        }
        inputSym = fileReader.GetSym();
        if (!(Token.whitespaces.contains(inputSym) || Character.isLetterOrDigit(inputSym))) {
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

    public int getNumVal() {
        return number;
    }

    private int number; // the last number encountered
    public int id; // the last identifier encountered
    private int idCounter = 3;

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

    public String getStringfromID(int id) {
        return IDToString.get(id);
    }
}
