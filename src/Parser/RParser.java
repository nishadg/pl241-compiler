package Parser;

import IR.CFG;
import IR.Converter;
import IR.SSAManager;
import IR.ScopeManager;
import Model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static Parser.Token.*;

public class RParser {
    private RScanner rScanner;
    private int sym; // the current token on the input

    public RParser(String fileName) throws IOException {
        rScanner = new RScanner(fileName);
        CFG.INSTANCE.setName(fileName);
    }

    public void parse() {
        sym = rScanner.getSym();
        parseToken("main");
        Converter.INSTANCE.createFunctionBlock(ScopeManager.MAIN_SCOPE);
        ScopeManager.INSTANCE.createScope(ScopeManager.MAIN_SCOPE);
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
            error("Expected " + token + " at line " + rScanner.getLineNum());
        }
    }

    private void parseToken(String token) {
        checkToken(token);
        nextSym();
    }

    private void parseFuncDecl() {
        rScanner.declareMode = true;
        while (sym == Token.funcToken || sym == Token.procToken) {
            //Function name
            nextSym();
            checkIdent();
            String functionName = rScanner.getCurrentToken();
            Converter.INSTANCE.createFunctionBlock(functionName);
            ScopeManager.INSTANCE.createScope(functionName);

            //Function arguments
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

            //Function body
            rScanner.declareMode = false;
            parseFuncBody();
            parseToken(";");
            Converter.INSTANCE.backToMain();
            ScopeManager.INSTANCE.backToMain();
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
        Result returnVal = parseExpression();
        Converter.INSTANCE.returnFromFunction(returnVal);
    }

    private void parseWhileStatement() {
        nextSym();
        BasicBlock parent = Converter.INSTANCE.getCurrentBlock();
        if(parent.getInstructionList().size() != 0){
            parent = Converter.INSTANCE.createChildOfCurrentBlock();
        }
        BasicBlock leftBlock = Converter.INSTANCE.createLeftBlockFor(parent);
        BasicBlock rightBlock = Converter.INSTANCE.createRightBlockFor(parent);
        BasicBlock joinBlock = Converter.INSTANCE.createWhileJoinBlock();
        Converter.INSTANCE.setCurrentBlock(joinBlock);
        int loopBackAddress = Instruction.getCounter();
        Condition x = parseRelation();
        Value y = Converter.INSTANCE.branchOnCondition(x);
        Converter.INSTANCE.setCurrentBlock(leftBlock);

        parseToken("do");
        parseStatSequence();
        Value end = Converter.INSTANCE.branch();
        end.location = loopBackAddress;
        parseToken("od");

        y.location = Instruction.getCounter();
        leftBlock = Converter.INSTANCE.getCurrentBlock();
        Converter.INSTANCE.fixupWhileJoinBlock(leftBlock, joinBlock);
        Converter.INSTANCE.setCurrentBlock(rightBlock);
    }

    private void parseIfStatement() {
        // Parse condition
        nextSym();
        Condition x = parseRelation();
        Value y = Converter.INSTANCE.branchOnCondition(x);
        SSAManager.INSTANCE.pushToPhiStack();
        boolean parentBranch = SSAManager.INSTANCE.leftBranch;

        // Parse Left
        parseToken("then");
        BasicBlock parent = Converter.INSTANCE.getCurrentBlock();
        BasicBlock leftBlock = Converter.INSTANCE.createLeftBlockFor(parent);
        Converter.INSTANCE.setCurrentBlock(leftBlock);
        SSAManager.INSTANCE.leftBranch = true;
        parseStatSequence();
        leftBlock = Converter.INSTANCE.getCurrentBlock();

        // Check and parse right
        BasicBlock rightBlock;
        if (sym == Token.elseToken) {
            Value end = Converter.INSTANCE.branch();
            y.location = Instruction.getCounter();
            nextSym();
            rightBlock = Converter.INSTANCE.createRightBlockFor(parent);
            SSAManager.INSTANCE.leftBranch = false;
            Converter.INSTANCE.setCurrentBlock(rightBlock);
            parseStatSequence();
            rightBlock = Converter.INSTANCE.getCurrentBlock();
            end.location = Instruction.getCounter();
        } else {
            rightBlock = parent;
            y.location = Instruction.getCounter();
        }

        // Join left and right
        BasicBlock joinBlock = Converter.INSTANCE.createIfJoinBlock(leftBlock, rightBlock);
        Converter.INSTANCE.setCurrentBlock(joinBlock);
        SSAManager.INSTANCE.addPhiInstructionsToCurrentBlock();
        SSAManager.INSTANCE.leftBranch = parentBranch; // restore to parent branch value (left/right)
        parseToken("fi");
    }

    private Condition parseRelation() {
        Result x = parseExpression();
        int code = parseRelOp();
        Result y = parseExpression();
        return Converter.INSTANCE.compare(code, x, y);
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
        checkIdent();
        String functionName = rScanner.getCurrentToken();
        nextSym();
        switch (rScanner.id) {
            case 0:
                return parseInputNum();
            case 1:
                return parseOutputNum();
            case 2:
                return parseOutputNewLine();
        }
        if (sym == Token.openparenToken) {
            do {
                nextSym();
                if (sym != Token.closeparenToken) {
                    parseExpression();
                }
            } while (sym == Token.commaToken);
            parseToken(")");
        }
        return Converter.INSTANCE.callFunction(ScopeManager.INSTANCE.findTokenInScope(functionName));
    }

    private Result parseOutputNewLine() {
        Converter.INSTANCE.newLine();
        parseToken("(");
        parseToken(")");
        return null;
    }

    private Result parseOutputNum() {
        parseToken("(");
        Result x = parseExpression();
        parseToken(")");
        Converter.INSTANCE.output(x);
        return x;
    }

    private Result parseInputNum() {
        Converter.INSTANCE.input();
        parseToken("(");
        parseToken(")");
        return new Value(Instruction.getCounter());
    }

    private void parseAssignment() {
        nextSym();
        Variable x = parseDesignator();
        parseToken("<-");
        Result y = parseExpression();
        x.assignmentLocation = Converter.INSTANCE.assign(x, y);
        SSAManager.INSTANCE.addValueInstance(x);
    }

    private Result parseExpression() {
        Result x = parseTerm();
        while (sym == Token.plusToken || sym == Token.minusToken) {
            int op = sym;
            nextSym();
            Result y = parseTerm();
            x = Converter.INSTANCE.compute(op, x, y);
        }
        return x;
    }

    private Result parseTerm() {
        Result x = parseFactor();
        while (sym == Token.timesToken || sym == Token.divToken) {
            int op = sym;
            nextSym();
            Result y = parseFactor();
            x = Converter.INSTANCE.compute(op, x, y);
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
                f = new Constant(rScanner.getNumVal());
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

    private Variable parseDesignator() {
        checkIdent();
        Variable var = SSAManager.INSTANCE.getCurrentValueInstance(rScanner.getCurrentToken());
        nextSym();
        while (sym == Token.openbracketToken) {
            nextSym();
            Result index = parseExpression();
            assert var != null;
            var.indices.add(index);
            parseToken("]");
        }
        return var;
    }


    private void parseVarDecl() {
        rScanner.declareMode = true;
        while (sym == Token.arrToken || sym == Token.varToken) {
            List<Integer> dimensions = parseTypeDecl();
            checkIdent();
            while (sym == ident) {
                Variable var = ScopeManager.INSTANCE.findTokenInScope(rScanner.getCurrentToken());
                assert var != null;
                var.dimensions = new ArrayList<>(dimensions);
                nextSym();
                if (sym == commaToken) {
                    nextSym();
                }
            }
            parseToken(";");
        }
        rScanner.declareMode = false;
    }

    private List<Integer> parseTypeDecl() {
        List<Integer> dimensions = new ArrayList<>();
        switch (sym) {
            case Token.arrToken:
                nextSym();
                checkToken("[");
                while (sym == Token.openbracketToken) {
                    nextSym();
                    if (sym != Token.number) {
                        error("Expected number");
                    }
                    dimensions.add(rScanner.getNumVal());
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
        return dimensions;
    }

    private void error(String s) {
        System.out.println("Line " + rScanner.getLineNum() + ": " + s);
        System.exit(1);
    }

    private void nextSym() {  // advance to the next token
        sym = rScanner.getSym();
    }
}
