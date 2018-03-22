package Model;

import IR.CFG;
import IR.Converter;
import IR.ScopeManager;

import java.util.*;

import static Model.Operation.load;

public class BasicBlock extends Result {
    private static int counter = 0;

    public int blockIndex;
    public String name; // name of the function, set when creating a new function block.

    // SSA fields
    public List<Variable> assignedVariables = new ArrayList<>();
    private List<Instruction> instructionList = new ArrayList<>();

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    // CFG fields
    public List<BasicBlock> parents = new ArrayList<>();
    public BasicBlock ifParentBlock;
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public boolean isIfJoin = false;
    public boolean isWhileJoin = false;

    // CSE fields
    HashMap<Operation, List<Instruction>> anchor = new HashMap<>();

    // RA fields
    public boolean isIfParent, otherSubtreeParsed;
    public HashSet<Integer> otherSubtreeLiveValues;

    // DLX fields
    public int programCounter;
    public int branchPCAddress;
    public List<BasicBlock> pendingBranchUpdates = new ArrayList<>();

    private BasicBlock() {
        super(Kind.ADDR);
        blockIndex = counter++;
    }

    public static BasicBlock create() {
        BasicBlock b = new BasicBlock();
        CFG.INSTANCE.addToCurrentGraph(b);
        return b;
    }

    @Override
    public String toString() {
        return "[" + blockIndex + "]";
    }

    public static void resetCounter() {
        counter = 0;
    }

    public Instruction addInstruction(Instruction i) {
        searchInAnchor(i);
        instructionList.add(i);
        i.containingBlock = this;
        return i;
    }

    public Instruction addInstructionToStart(Instruction i) {
        assert i.op == Operation.phi;
        addToAnchor(i);
        instructionList.add(0, i);
        i.containingBlock = this;
        return i;
    }

    public Instruction getLastInstruction() {
        return instructionList.get(instructionList.size() - 1);
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


    // CSE functions
    public void addToAnchor(Instruction i) {
        Operation anchorOp = i.op;
        if (!Operation.nonAnchored.contains(i.op)) {
            if (i.op == Operation.store || i.op == Operation.phi || i.op == Operation.call) {  // killing inanchors
                anchorOp = load;
            }
            if (!anchor.containsKey(anchorOp)) anchor.put(anchorOp, new ArrayList<>());
            anchor.get(anchorOp).add(i);
        }
    }

    public void inheritAnchor(BasicBlock fromBlock) {
        anchor = new HashMap<>();
        for (Map.Entry<Operation, List<Instruction>> entry : fromBlock.anchor.entrySet()) {
            anchor.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    private void searchInAnchor(Instruction i) {
        int index = -1;
        if (anchor.containsKey(i.op)) {
            if (i.op == load) { // need to break if same operand is stored
                List<Instruction> instructions = anchor.get(load);
                for (int anchorIndex = instructions.size() - 1; anchorIndex >= 0; anchorIndex--) {
                    Instruction anchoredInstruction = instructions.get(anchorIndex);
                    if (anchoredInstruction.op == Operation.call && i.containsGlobals()) {
                        break;
                    } else if (anchoredInstruction.op == Operation.store && anchoredInstruction.y.equals(i.x)) {
                        i.propagateCopy(anchoredInstruction.x);
                        break;
                    } else if (anchoredInstruction.op == Operation.phi && i.x.kind == Kind.VAR &&
                            ((anchoredInstruction.number == ((Variable) i.x).getValueLocation().number) ||
                                    anchoredInstruction.number == ((Variable) i.x).assignmentLocation.number)) {
                        i.propagateCopy(anchoredInstruction);
                        break;
                    } else if (i.x.equals(anchoredInstruction.x)) {
                        index = anchorIndex;
                        break;
                    }
                }
            } else {
                index = anchor.get(i.op).indexOf(i);
            }
            if (index == -1) addToAnchor(i);
            else {
                i.isDeleted = true;
                i.deletedBecause = Instruction.DeleteReason.CSE;
                i.replacementInstruction = anchor.get(i.op).get(index);
            }

        } else {
            addToAnchor(i);
        }
    }

    public void initAnchor() {
        anchor = new HashMap<>();
    }

    public void recheckAnchors() {
        leftBlock.inheritAndSearchAgain(this, this);
    }

    private void inheritAndSearchAgain(BasicBlock parent, BasicBlock join) {
        inheritAnchor(parent);
        for (Instruction i : instructionList) {
                searchInAnchor(i);
        }
        if (leftBlock == join) return;
        if (leftBlock != null) {
            if (leftBlock.isWhileJoin)
                leftBlock.recheckAnchors();
            else if (leftBlock.isIfJoin)
                leftBlock.inheritAndSearchAgain(leftBlock.ifParentBlock, join);
            else
                leftBlock.inheritAndSearchAgain(this, join);
        }
        if (rightBlock != null) {
            if (rightBlock.isIfJoin)
                rightBlock.inheritAndSearchAgain(rightBlock.ifParentBlock, join);
            else
                rightBlock.inheritAndSearchAgain(this, join);
        }
    }

    public boolean areBranchUpdatesPending() {
        return !pendingBranchUpdates.isEmpty();
    }
}
