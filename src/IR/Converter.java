package IR;

import Model.*;
import Parser.Token;

import java.util.ArrayList;
import java.util.Map;

import static Model.Kind.*;

public enum Converter {
    INSTANCE;

    private BasicBlock currentBlock;
    private Map<Integer, Operation> tokenOpMap = Map.ofEntries(
            Map.entry(Token.plusToken, Operation.add),
            Map.entry(Token.minusToken, Operation.sub),
            Map.entry(Token.divToken, Operation.div),
            Map.entry(Token.timesToken, Operation.mul)
    );
    private Map<Integer, Operation> tokenOpImmMap = Map.ofEntries(
            Map.entry(Token.plusToken, Operation.addi),
            Map.entry(Token.minusToken, Operation.subi),
            Map.entry(Token.divToken, Operation.divi),
            Map.entry(Token.timesToken, Operation.muli)
    );

    public void compute(int op, Result x, Result y) {
        if (x.kind == Kind.CONST && y.kind == Kind.CONST) {
            switch (op) {
                case Token.plusToken:
                    x.value += y.value;
                    break;
                case Token.minusToken:
                    x.value -= y.value;
                    break;
                case Token.timesToken:
                    x.value *= y.value;
                    break;
                case Token.divToken:
                    x.value /= y.value;
                    break;
                default:
                    System.out.print("Invalid operation");
            }
        } else {
            load(x);
            Instruction i;
            if (y.kind == Kind.CONST) {
                i = new Instruction(tokenOpImmMap.get(op), x, y);
            } else {
                load(y);
                i = new Instruction(tokenOpMap.get(op), x, y);
            }
            currentBlock.addInstruction(i);
        }
    }

    void load(Result x) {
        if (x.kind == VAR) {
            Instruction i = new Instruction(Operation.load, x);
            x.regNo = i.getNumber();
            x.kind = REG;
        } else if (x.kind == CONST) {
            if (x.value == 0) x.regNo = 0;
            else {
                Result x1 = new Result(REG, 0);
                Instruction i = new Instruction(Operation.addi, x1, x);
                currentBlock.addInstruction(i);
                x.regNo = i.getNumber();
            }
            x.kind = REG;
        }
    }

    public void assign(Result x, Result y) { // let x <= y;
        if (y.kind == CONST) {
            Result x1 = new Result(REG, 0);
            Instruction i = new Instruction(Operation.addi, x1, y);
            x1.regNo = i.getNumber();
            currentBlock.addInstruction(i);
            currentBlock.addInstruction(new Instruction(Operation.move, x1, x));
        } else {
            currentBlock.addInstruction(new Instruction(Operation.move, y, x));
        }
    }

    public Result compare(int op, Result x, Result y) {
        x.cond = op;
        currentBlock.addInstruction(new Instruction(Operation.cmp, x, y));
        return x;
    }

    public Result branchOnCondition(Result x) {
        Instruction i = null;
        Result y = new Result(COND);
        switch (x.cond) {
            case Token.eqlToken:
                i = new Instruction(Operation.bne, x, y);
                break;
            case Token.neqToken:
                i = new Instruction(Operation.beq, x, y);
                break;
            case Token.lssToken:
                i = new Instruction(Operation.bge, x, y);
                break;
            case Token.geqToken:
                i = new Instruction(Operation.blt, x, y);
                break;
            case Token.leqToken:
                i = new Instruction(Operation.bgt, x, y);
                break;
            case Token.gtrToken:
                i = new Instruction(Operation.ble, x, y);
                break;
            default:
                System.out.print("Invalid condition");
        }
        currentBlock.addInstruction(i);

        return y;
    }

    public Result branch() {
        Result y = new Result(COND, 0);
        currentBlock.addInstruction(new Instruction(Operation.bra, y));
        return y;
    }

    public void end() {
        currentBlock.addInstruction(new Instruction(Operation.end));
        CFG.INSTANCE.createDotFile();
        CFG.INSTANCE.graphs = new ArrayList<>();
        BasicBlock.resetCounter();
        Instruction.resetCounter();
    }

    public void newLine() {
        currentBlock.addInstruction(new Instruction(Operation.writeNL));
    }

    public void output(Result x) {
        currentBlock.addInstruction(new Instruction(Operation.write, x));
    }

    public void input() {
        currentBlock.addInstruction(new Instruction(Operation.read));
    }

    public void createFunction(String name) {
        CFG.INSTANCE.addGraph();
        currentBlock = BasicBlock.create();
        currentBlock.name = name;
    }

    public void backToMain() {
        currentBlock = CFG.INSTANCE.resetCurrentGraphToMain();
    }

    public BasicBlock createLeftBlockFor(BasicBlock parent) {
        BasicBlock left = BasicBlock.create();
        left.parents.add(parent);
        parent.leftBlock = left;
        return left;
    }


    public BasicBlock createRightBlockFor(BasicBlock parent) {
        BasicBlock right = BasicBlock.create();
        right.parents.add(parent);
        parent.rightBlock = right;
        return right;
    }

    public BasicBlock createIfJoinBlock(BasicBlock leftBlock, BasicBlock rightBlock) {
        BasicBlock joinBlock = BasicBlock.create();
        joinBlock.isJoin = true;
        leftBlock.leftBlock = joinBlock;
        if (rightBlock.leftBlock == null) // check else/fallthrough
            rightBlock.leftBlock = joinBlock;
        else
            rightBlock.rightBlock = joinBlock;
        joinBlock.parents.add(leftBlock);
        joinBlock.parents.add(rightBlock);
        return joinBlock;
    }

    public void joinCurrentBlockTo(BasicBlock joinBlock) {
        currentBlock.leftBlock = joinBlock;
    }

    public void setRight(BasicBlock parent, BasicBlock child) {
        parent.rightBlock = child;
        child.parents.add(parent);
    }

    public BasicBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(BasicBlock current) {
        this.currentBlock = current;
    }

    public BasicBlock createWhileJoinBlock() {
        BasicBlock joinBlock = currentBlock;
        joinBlock.isJoin = true;
        return joinBlock;
    }

    public void fixupWhileJoinBlock(BasicBlock leftBlock, BasicBlock joinBlock) {
        leftBlock.leftBlock = joinBlock;
        joinBlock.parents.add(leftBlock);
    }


}
