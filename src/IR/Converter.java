package IR;

import Model.*;
import Model.Register;
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
            Constant c = (Constant) x;
            x = c.compute(op, y);
        } else {
            x = load(x);
            Instruction i;
            if (y.kind == Kind.CONST) {
                i = new Instruction(tokenOpImmMap.get(op), x, y);
            } else {
                y = load(y);
                i = new Instruction(tokenOpMap.get(op), x, y);
            }
            currentBlock.addInstruction(i);
        }
    }

    Register load(Result x) {
        if (x.kind == VAR) {
            Instruction i = new Instruction(Operation.load, x);
            return new Register(i.getNumber());
        } else if (x.kind == CONST) {
            // TODO: Use R0 for 0 value
            Register r = new Register(0);
            Instruction i = new Instruction(Operation.addi, r, x);
            currentBlock.addInstruction(i);
            r.regNo = i.getNumber();
            return r;
        } else {
            return (Register) x;
        }
    }

    public void assign(Result x, Result y) { // let x <= y;
        if (y.kind == CONST) {
            Register r = new Register(0);
            Instruction i = new Instruction(Operation.addi, r, y);
            r.regNo = i.getNumber();
            currentBlock.addInstruction(i);
            currentBlock.addInstruction(new Instruction(Operation.move, r, x));
        } else {
            currentBlock.addInstruction(new Instruction(Operation.move, y, x));
        }
    }

    public Condition compare(int op, Result x, Result y) {
        currentBlock.addInstruction(new Instruction(Operation.cmp, x, y));
        return new Condition(op);
    }

    public Address branchOnCondition(Condition x) {
        Instruction i = null;
        Address y = new Address(0);
        switch (x.operator) {
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

    public Address branch() {
        Address y = new Address(0);
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
