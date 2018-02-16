package IR;

import Model.*;
import Parser.Token;

import java.util.ArrayList;
import java.util.Map;

import static Model.Kind.*;

public enum Converter {
    INSTANCE;

    private BasicBlock currentBlock;
    private ArrayList<Instruction> instructions = new ArrayList<>();
    private Map<Integer, Operation> tokenOpMap = Map.ofEntries(
            Map.entry(Token.plusToken, Operation.add),
            Map.entry(Token.minusToken, Operation.sub),
            Map.entry(Token.divToken, Operation.div),
            Map.entry(Token.timesToken, Operation.mul)
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
            if (y.kind != Kind.CONST) {
                load(y);
            }
            Instruction i = new Instruction(tokenOpMap.get(op), x, y);
            x.kind = REG;
            x.regNo = i.getNumber();
            instructions.add(i);
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
                Instruction i = new Instruction(Operation.add, x1, x);
                instructions.add(i);
                x.regNo = i.getNumber();
            }
            x.kind = REG;
        }
    }

    public void assign(Result x, Result y) {
        if (y.kind == CONST) {
            Result x1 = new Result(REG, 0);
            Instruction i = new Instruction(Operation.add, x1, y);
            x1.regNo = i.getNumber();
            instructions.add(i);
            instructions.add(new Instruction(Operation.move, x1, x));
        } else {
            instructions.add(new Instruction(Operation.move, y, x));
        }
    }

    public Result compare(int op, Result x, Result y) {
        x.cond = op;
        instructions.add(new Instruction(Operation.cmp, x, y));
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
        instructions.add(i);

        return y;
    }

    public Result branch() {
        Result y = new Result(COND, 0);
        instructions.add(new Instruction(Operation.bra, y));
        return y;
    }

    public void end() {
        instructions.add(new Instruction(Operation.end));
        CFG.INSTANCE.createDot();
    }

    public void newLine() {
        instructions.add(new Instruction(Operation.writeNL));
    }

    public void output(Result x) {
        instructions.add(new Instruction(Operation.write, x));
    }

    public void input() {
        instructions.add(new Instruction(Operation.read));
    }

    public void init() {
        CFG.INSTANCE.addHead();
        currentBlock = BasicBlock.create();
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

    public BasicBlock createJoinBlock(BasicBlock leftBlock, BasicBlock rightBlock) {
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

}
