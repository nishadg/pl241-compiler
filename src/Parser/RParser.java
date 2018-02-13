package Parser;

import java.io.IOException;

import static Parser.Token.*;

public class RParser {
    private RScanner rScanner;
    private int sym; // the current token on the input

    public RParser(String fileName) throws IOException {
        rScanner = new RScanner(fileName);
    }

    public void parse() {
        sym = rScanner.getSym();
        parseToken("main");
        parseVarDecl();
        parseFuncDecl();
        parseToken("{");
        parseStatSequence();
        parseToken("}");
        parseToken(".");
        System.out.println("Parsed successfully!");
    }

    private void checkIdent() {
        if (sym != Token.ident) {
            error("Expected identifier");
        }
    }

    private void parseIdent() {
        checkIdent();
        nextSym();
    }

    private void checkToken(String token) {
        if (sym != Token.tokenValueMap.get(token)) {
            error("Expected ".concat(token));
        }
    }

    private void parseToken(String token) {
        checkToken(token);
        nextSym();
    }

    private void parseFuncDecl() {
        while (sym == Token.funcToken || sym == Token.procToken) {
            nextSym();
            checkIdent();
            nextSym();
            if (sym == openparenToken) {
                nextSym();
                while (sym == ident) { // TODO: compare with parseVarDecl for identifier sequence
                    nextSym();
                    if (sym == commaToken) {
                        nextSym();
                    }
                }
                parseToken(")");
            }
            parseToken(";");
            parseFuncBody();
            parseToken(";");
        }
    }

    private void parseFuncBody() {
        parseVarDecl();
        parseToken("{");
        if (sym != endToken) {
            parseStatSequence();
        }
        parseToken("}");
    }

    private void parseStatSequence() {
        parseStatement();
        while (sym == Token.semiToken) {
            nextSym();
            parseStatement();
        }
    }

    private void parseStatement() {
        switch (sym) {
            case Token.letToken:
                parseAssignment();
                break;
            case Token.callToken:
                parseFuncCall();
                break;
            case Token.ifToken:
                parseIfStatement();
                break;
            case Token.whileToken:
                parseWhileStatement();
                break;
            case Token.returnToken:
                parseReturnStatement();
                break;
            default:
                error("Unexpected token when parsing statement.");
        }
    }

    private void parseReturnStatement() {
        nextSym();
        parseExpression();
    }

    private void parseWhileStatement() {
        nextSym();
        parseRelation();
        parseToken("do");
        parseStatSequence();
        parseToken("od");
    }

    private void parseIfStatement() {
        nextSym();
        parseRelation();
        parseToken("then");
        parseStatSequence();
        if (sym == Token.elseToken) {
            nextSym();
            parseStatSequence();
        }
        parseToken("fi");
    }

    private void parseRelation() {
        parseExpression();
        parseRelOp();
        parseExpression();
    }

    private void parseRelOp() {
        if (sym >= Token.eqlToken && sym <= Token.gtrToken) {
            nextSym();
        } else {
            error("Expected relation operator");
        }
    }

    private void parseFuncCall() {
        nextSym();
        parseIdent();
        if (sym == Token.openparenToken) {
            do {
                nextSym();
                if (sym != Token.closeparenToken) {
                    parseExpression();
                }
            } while (sym == Token.commaToken);
            parseToken(")");
        }
    }

    private void parseAssignment() {
        nextSym();
        parseDesignator();
        parseToken("<-");
        parseExpression();
    }

    private void parseExpression() {
        parseTerm();
        while (sym == Token.plusToken || sym == Token.minusToken) {
            nextSym();
            parseTerm();
        }
    }

    private void parseTerm() {
        parseFactor();
        while (sym == Token.timesToken || sym == Token.divToken) {
            nextSym();
            parseFactor();
        }
    }

    private void parseFactor() {
        switch (sym) {
            case Token.ident:
                parseDesignator();
                break;
            case Token.number:
                nextSym();
                break;
            case Token.openparenToken:
                nextSym();
                parseExpression();
                parseToken(")");
                break;
            case Token.callToken:
                parseFuncCall();
                break;
            default:
                error("Invalid factor");
        }
    }

    private void parseDesignator() {
        parseIdent();
        while (sym == Token.openbracketToken) {
            nextSym();
            parseExpression();
            parseToken("]");
        }
    }


    private void parseVarDecl() {
        while (sym == Token.arrToken || sym == Token.varToken) {
            parseTypeDecl();
            checkIdent();
            while (sym == ident) {
                nextSym();
                if (sym == commaToken) {
                    nextSym();
                }
            }
            parseToken(";");
        }
    }

    private void parseTypeDecl() {
        switch (sym) {
            case Token.arrToken:
                nextSym();
                checkToken("[");
                while (sym == Token.openbracketToken) {
                    nextSym();
                    if (sym != Token.number) {
                        error("Expected number");
                    }
                    nextSym();
                    parseToken("]");
                }
                break;
            case Token.varToken:
                nextSym();
                break;
            default:
                error("Expected 'var' or 'array'");
                break;
        }
    }

    private void error(String s) {
        System.out.println("Line " + rScanner.getLineNum() + ": " + s);
        System.exit(1);
    }

    private void nextSym() {  // advance to the next token
        sym = rScanner.getSym();
    }
}
