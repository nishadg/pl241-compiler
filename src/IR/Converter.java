package IR;

import Model.*;
import Model.Register;
import Parser.Token;

import java.util.ArrayList;
import java.util.Map;

import static Model.Kind.*;

public class Converter {

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

    public Result compute(int op, Result x, Result y) {
        if (x.kind == Kind.CONST && y.kind == Kind.CONST) {
            Constant c = (Constant) x;
            x = c.compute(op, y);
        } else {
//            x = load(x);
            Instruction i;
//            if (y.kind == Kind.CONST) {
//                i = new Instruction(tokenOpImmMap.get(op), x, y);
//            } else {
//                y = load(y);
                i = new Instruction(tokenOpMap.get(op), x, y);
//            }
            x = currentBlock.addInstruction(i);
        }
        return x;
    }

    Result load(Result x) {
        if (x.kind == VAR) {
            Instruction i = new Instruction(Operation.load, x);
            return currentBlock.addInstruction(i);
        } else if (x.kind == CONST) {
            // TODO: Use R0 for 0 value
            Register r = new Register(0);
            Instruction i = new Instruction(Operation.addi, r, x);
            return currentBlock.addInstruction(i);
        } else {
            return x;
        }
    }

    public Instruction assign(Result x, Result y) { // let x <= y;
//        if (y.kind == CONST) {
//            Register r = new Register(0);
//            Instruction i = new Instruction(Operation.addi, r, y);
//            Value a = currentBlock.addInstruction(i);
//            return currentBlock.addInstruction(new Instruction(Operation.move, a, x));
//        } else {
            return currentBlock.addInstruction(new Instruction(Operation.move, y, x));
//        }
    }

    public Instruction phi(Variable old, Variable left, Variable right){
        Instruction i = new Instruction(Operation.phi, left, right);
        i.setPhiInstruction(old);
        return currentBlock.addInstruction(i);
    }

    public Instruction whilePhi(Variable old, Variable left, Variable right){
        Instruction i = new Instruction(Operation.phi, left, right);
        i.setPhiInstruction(old);
        return currentBlock.addInstructionToStart(i);
    }

    public Condition compare(int op, Result x, Result y) {
        Instruction a = currentBlock.addInstruction(new Instruction(Operation.cmp, x, y));
        return new Condition(op, a);
    }

    public void branchOnCondition(Condition x) {
        Instruction i = null;
        switch (x.operator) {
            case Token.eqlToken:
                i = new Instruction(Operation.bne, x.compareLocation);
                break;
            case Token.neqToken:
                i = new Instruction(Operation.beq, x.compareLocation);
                break;
            case Token.lssToken:
                i = new Instruction(Operation.bge, x.compareLocation);
                break;
            case Token.geqToken:
                i = new Instruction(Operation.blt, x.compareLocation);
                break;
            case Token.leqToken:
                i = new Instruction(Operation.bgt, x.compareLocation);
                break;
            case Token.gtrToken:
                i = new Instruction(Operation.ble, x.compareLocation);
                break;
            default:
                System.out.print("Invalid condition");
        }
        currentBlock.addInstruction(i);
    }

    public void branch() {
        currentBlock.addInstruction(new Instruction(Operation.bra));
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

    public Instruction input() {
        return currentBlock.addInstruction(new Instruction(Operation.read));
    }

    public void createFunctionBlock(String name) {
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


    public BasicBlock createChildOfCurrentBlock() {
        BasicBlock child = createLeftBlockFor(currentBlock);
        setCurrentBlock(child);
        return child;
    }

    public Instruction callFunction(Variable functionName) {
        return currentBlock.addInstruction(new Instruction(Operation.call, functionName));
    }

    public void returnFromFunction(Result returnVal) {
        currentBlock.addInstruction(new Instruction(Operation.ret, returnVal));
    }
}
