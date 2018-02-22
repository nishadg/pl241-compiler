package Model;

import IR.CFG;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Result{
    static int counter = 0;

    public int index;

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    List<Instruction> instructionList = new ArrayList<>();
    public List<BasicBlock> parents = new ArrayList<>();
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public boolean isJoin = false;
    public String name; // name of the function, set when creating a new function block.

    private BasicBlock() {
        super(Kind.ADDR);
        index = counter++;
    }

    public static BasicBlock create(){
        BasicBlock b = new BasicBlock();
        CFG.INSTANCE.addToCurrentGraph(b);
        return b;
    }

    public static void resetCounter(){
        counter = 0;
    }

    public Instruction addInstruction(Instruction i) {
        instructionList.add(i);
        return i;
    }
    public Instruction addInstructionToStart(Instruction i) {
        instructionList.add(0,i);
        return i;
    }

    public Instruction getLastInstruction() {
        return instructionList.get(instructionList.size() - 1);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
