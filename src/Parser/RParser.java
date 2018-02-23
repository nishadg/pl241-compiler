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
    private SSAManager ssaManager;
    private Converter converter;
    private int sym; // the current token on the input

    public RParser(String fileName) throws IOException {
        rScanner = new RScanner(fileName);
        converter = new Converter();
        ssaManager = new SSAManager();
        CFG.INSTANCE.setName(fileName);
    }

    public void parse() {
        sym = rScanner.getSym();
        parseToken("main");
        converter.init();
        parseVarDecl();
        parseFuncDecl();
        parseToken("{");
        parseStatSequence();
        parseToken("}");
        parseToken(".");
        converter.end();
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
            Variable function = ScopeManager.INSTANCE.findTokenInScope(functionName);
            assert function != null;
            function.isFunction = true;
            converter.createFunctionBlock(functionName);
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
            converter.backToMain();
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
        converter.returnFromFunction(returnVal);
    }

    private void parseWhileStatement() {
        nextSym();

        // Create join block
        BasicBlock parent = converter.getCurrentBlock();
        if(parent.getInstructionList().size() != 0){
            parent = converter.createChildOfCurrentBlock();
        }
        BasicBlock leftBlock = converter.createLeftBlockFor(parent);
        BasicBlock rightBlock = converter.createRightBlockFor(parent);
        BasicBlock joinBlock = converter.createWhileJoinBlock();
        converter.setCurrentBlock(joinBlock);
        ssaManager.pushToPhiStack();
        boolean parentBranch = ssaManager.leftBranch;

        // Parse Condition
        Condition x = parseRelation();
        converter.branchOnCondition(x);
        converter.setCurrentBlock(leftBlock);

        // Parse loop
        ssaManager.leftBranch = false;
        parseToken("do");
        parseStatSequence();
        converter.branch();

        // Finish loop
        parseToken("od");
        leftBlock = converter.getCurrentBlock();
        converter.fixupWhileJoinBlock(leftBlock, joinBlock);
        ssaManager.leftBranch = parentBranch;
        ssaManager.addPhiForWhile(converter, joinBlock);

        // fix branch addresses
        joinBlock.leftBlock.getLastInstruction().setX(joinBlock);
        joinBlock.getLastInstruction().setY(joinBlock.rightBlock);

        // proceed
        converter.setCurrentBlock(rightBlock);
    }

    private void parseIfStatement() {
        BasicBlock parent = converter.getCurrentBlock();
        BasicBlock leftBlock = converter.createLeftBlockFor(parent);
        BasicBlock joinBlock;

        // Parse condition
        nextSym();
        Condition x = parseRelation();
        converter.branchOnCondition(x);
        ssaManager.pushToPhiStack();
        boolean parentBranch = ssaManager.leftBranch;

        // Parse Left
        parseToken("then");
        converter.setCurrentBlock(leftBlock);
        ssaManager.leftBranch = true;
        parseStatSequence();
        leftBlock = converter.getCurrentBlock();

        // Check and parse right
        BasicBlock rightBlock;
        if (sym == Token.elseToken) {
            converter.branch();
            nextSym();
            rightBlock = converter.createRightBlockFor(parent);

            // fix branch for parent
            parent.getLastInstruction().setY(rightBlock);

            // parse else (right) block
            ssaManager.leftBranch = false;
            converter.setCurrentBlock(rightBlock);
            parseStatSequence();

            // Join left and right
            joinBlock = converter.createIfJoinBlock(leftBlock, converter.getCurrentBlock(), parent);

            // fix branch for left block
            leftBlock.getLastInstruction().setX(joinBlock);
        } else {
            // Join left and parent
            joinBlock = converter.createIfJoinBlock(leftBlock, parent, parent);

            // fix branch for parent
            parent.getLastInstruction().setY(joinBlock);
        }


        converter.setCurrentBlock(joinBlock);
        ssaManager.leftBranch = parentBranch; // restore to parent branch value (left/right)
        ssaManager.addPhiInstructionsForIf(converter);

        // proceed
        parseToken("fi");
    }

    private Condition parseRelation() {
        Result x = parseExpression();
        int code = parseRelOp();
        Result y = parseExpression();
        return converter.compare(code, x, y);
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
        return converter.callFunction(ScopeManager.INSTANCE.findTokenInScope(functionName));
    }

    private Result parseOutputNewLine() {
        converter.newLine();
        parseToken("(");
        parseToken(")");
        return null;
    }

    private Result parseOutputNum() {
        parseToken("(");
        Result x = parseExpression();
        parseToken(")");
        converter.output(x);
        return x;
    }

    private Result parseInputNum() {
        parseToken("(");
        parseToken(")");
        return converter.input();
    }

    private void parseAssignment() {
        nextSym();
        Variable x = parseDesignator(true);
        parseToken("<-");
        Result y = parseExpression();
        x.assignmentLocation = converter.assign(x, y);
        ssaManager.addValueInstance(x, converter.getCurrentBlock());
    }

    private Result parseExpression() {
        Result x = parseTerm();
        while (sym == Token.plusToken || sym == Token.minusToken) {
            int op = sym;
            nextSym();
            Result y = parseTerm();
            x = converter.compute(op, x, y);
        }
        return x;
    }

    private Result parseTerm() {
        Result x = parseFactor();
        while (sym == Token.timesToken || sym == Token.divToken) {
            int op = sym;
            nextSym();
            Result y = parseFactor();
            x = converter.compute(op, x, y);
        }
        return x;
    }

    private Result parseFactor() {
        Result f;
        switch (sym) {
            case Token.ident:
                f = parseDesignator(false);
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

    private Variable parseDesignator(boolean isDef) {
        checkIdent();
        Variable var = ssaManager.getCurrentValueInstance(rScanner.getCurrentToken(), converter.getCurrentBlock(), isDef);
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
