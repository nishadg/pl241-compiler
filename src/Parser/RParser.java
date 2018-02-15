package Parser;

import Machine.Converter;
import Model.Instruction;
import Model.Kind;
import Model.Operation;
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
        Converter.INSTANCE.end();
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
        Result x = parseRelation();
        parseToken("then");
        Result y = Converter.INSTANCE.branchOnCondition(x);
        parseStatSequence();
        if (sym == Token.elseToken) {
            Result end = Converter.INSTANCE.branch();
            y.fixuplocation = Instruction.getCounter();
            nextSym();
            parseStatSequence();
        } else {
            y.fixuplocation = Instruction.getCounter();
        }
        parseToken("fi");
    }

    private Result parseRelation() {
        Result x = parseExpression();
        int code = parseRelOp();
        Result y = parseExpression();
        Converter.INSTANCE.compare(code, x, y);
        return x;
    }

    private int parseRelOp() {
        int code;
        if (sym >= Token.eqlToken && sym <= Token.gtrToken) {
            code = sym;
            nextSym();
        } else {
            code = 0;
            error("Expected relation operator");
        }
        return code;
    }

    private Result parseFuncCall() {
        nextSym();
        parseIdent();
        switch (rScanner.id) {
            case 0:
                return parseInputNum();
            case 1:
                return parseOutputNum();
            case 2:
                return parseOutputNewLine();
        }
        Result f = new Result(Kind.VAR, rScanner.id);
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

    private Result parseOutputNewLine() {
        Converter.INSTANCE.instructions.add(new Instruction(Operation.writeNL));
        return null;
    }

    private Result parseOutputNum() {
        parseToken("(");
        Result x = parseExpression();
        parseToken(")");
        Converter.INSTANCE.instructions.add(new Instruction(Operation.write, x));
        return x;
    }

    private Result parseInputNum() {
        Converter.INSTANCE.instructions.add(new Instruction(Operation.read));
        return new Result(Kind.VAR, 0);
    }

    private void parseAssignment() {
        nextSym();
        Result x = parseDesignator();
        parseToken("<-");
        Result y = parseExpression();
        Converter.INSTANCE.assign(x, y);
    }

    private Result parseExpression() {
        Result x = parseTerm();
        while (sym == Token.plusToken || sym == Token.minusToken) {
            int op = sym;
            nextSym();
            Result y = parseTerm();
            Converter.INSTANCE.compute(op, x, y);
        }
        return x;
    }

    private Result parseTerm() {
        Result x = parseFactor();
        while (sym == Token.timesToken || sym == Token.divToken) {
            int op = sym;
            nextSym();
            Result y = parseFactor();
            Converter.INSTANCE.compute(op, x, y);
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
                f = new Result(Kind.CONST, rScanner.getNumVal());
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
        Result d = new Result(Kind.VAR, rScanner.id);
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
