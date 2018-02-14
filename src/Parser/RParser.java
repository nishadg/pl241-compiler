package Parser;

import Machine.Instructions;
import Model.Instruction;
import Model.KIND;
import Model.Result;

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
                while (sym == ident) {
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

    private Result parseFuncCall() {
        nextSym();
        parseIdent();
        Result f = new Result(KIND.VAR, rScanner.id);
        if (sym == Token.openparenToken) {
            do {
                nextSym();
                if (sym != Token.closeparenToken) {
                    parseExpression();
                }
            } while (sym == Token.commaToken);
            parseToken(")");
        }
        return f;
    }

    private void parseAssignment() {
        nextSym();
        parseDesignator();
        parseToken("<-");
        parseExpression();
    }

    private Result parseExpression() {
        Result x = parseTerm();
        while (sym == Token.plusToken || sym == Token.minusToken) {
            nextSym();
            Result y = parseTerm();
            Instructions.compute(sym, x, y);
        }
        return x;
    }

    private Result parseTerm() {
        Result x = parseFactor();
        while (sym == Token.timesToken || sym == Token.divToken) {
            nextSym();
            Result y = parseFactor();
            Instructions.compute(sym, x, y);
        }
        return x;
    }

    private Result parseFactor() {
        Result f;
        switch (sym) {
            case Token.ident:
                f = parseDesignator();
                break;
            case Token.number:
                f = new Result(KIND.CONST, rScanner.getNumVal());
                nextSym();
                break;
            case Token.openparenToken:
                nextSym();
                f = parseExpression();
                parseToken(")");
                break;
            case Token.callToken:
                f = parseFuncCall();
                break;
            default:
                f = null;
                error("Invalid factor");
        }
        return f;
    }

    private Result parseDesignator() {
        parseIdent();
        Result d = new Result(KIND.VAR, rScanner.id);
        while (sym == Token.openbracketToken) {
            nextSym();
            parseExpression();
            parseToken("]");
        }
        return d;
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
