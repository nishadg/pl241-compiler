package Model;

import IR.CFG;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Result {
    static int counter = 0;

    public int index;

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public List<Variable> assignedVariables = new ArrayList<>();

    List<Instruction> instructionList = new ArrayList<>();
    public List<BasicBlock> parents = new ArrayList<>();
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public boolean isIfJoin = false;
    public boolean isWhileJoin = false;
    public String name; // name of the function, set when creating a new function block.

    private BasicBlock() {
        super(Kind.ADDR);
        index = counter++;
    }

    public static BasicBlock create() {
        BasicBlock b = new BasicBlock();
        CFG.INSTANCE.addToCurrentGraph(b);
        return b;
    }

    public static void resetCounter() {
        counter = 0;
    }

    public Instruction addInstruction(Instruction i) {
        instructionList.add(i);
        return i;
    }

    public Instruction addInstructionToStart(Instruction i) {
        instructionList.add(0, i);
        return i;
    }

    public Instruction getLastInstruction() {
        return instructionList.get(instructionList.size() - 1);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }

    public Variable getOldAssignmentFromParent(Variable v) {
        if (parents.isEmpty()) {
            return v;
        } else if (isIfJoin) {
            return parents.get(0).parents.get(0).getOldAssignmentFromParent(v);
        } else {
            List<Variable> searchList = parents.get(0).assignedVariables;
            for (int i = searchList.size() - 1; i >= 0; i--) {
                Variable oldVar = searchList.get(i);
                if (v.getId() == oldVar.getId()) {
                    return oldVar.copy();
                }
            }
            return parents.get(0).getOldAssignmentFromParent(v);
        }
    }
}
