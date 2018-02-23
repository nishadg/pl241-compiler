package Model;

import IR.CFG;
import IR.Converter;
import IR.ScopeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasicBlock extends Result {
    static int counter = 0;

    public int index;

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public List<Variable> assignedVariables = new ArrayList<>();

    List<Instruction> instructionList = new ArrayList<>();
    public List<BasicBlock> parents = new ArrayList<>();
    public BasicBlock ifParentBlock;
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
            Variable currentVar = Objects.requireNonNull(ScopeManager.INSTANCE.findTokenInScope(v.getName())).copy();
            currentVar.assignmentLocation = Converter.initLocation;
            return currentVar;
        } else if (isIfJoin) {
            return ifParentBlock.getAssignment(v);
        } else {
            return parents.get(0).getAssignment(v);
        }
    }

    private Variable searchByID(List<Variable> searchList, Variable v) {
        for (int i = searchList.size() - 1; i >= 0; i--) {
            Variable oldVar = searchList.get(i);
            if (v.getId() == oldVar.getId()) {
                return oldVar.copy();
            }
        }
        return null;
    }

    public Variable getAssignment(Variable v) {
        Variable oldVar = searchByID(assignedVariables, v);
        return oldVar != null ? oldVar : getOldAssignmentFromParent(v);
    }
}
