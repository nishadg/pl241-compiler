package IR;

import Model.BasicBlock;
import Model.Instruction;
import Model.Variable;

import java.util.*;
import java.util.stream.Collectors;

public class SSAManager {

    class Phi {
        Variable old;
        Variable left;
        Variable right;
    }

    public boolean leftBranch = true;
    HashMap<Integer, Variable> varIDInstanceMap = new HashMap<>();
    HashMap<Variable, List<Variable>> varDefUseChain = new HashMap<>();
    Stack<List<Phi>> phiStack = new Stack<>();

    public void addValueInstance(Variable v, BasicBlock currentBlock) {
        // Create def in def-use chain
        varDefUseChain.put(v, new ArrayList<>());
        currentBlock.assignedVariables.add(v);

        if (!phiStack.empty()) { // we are in one of the branches. Create phi for outer join block.
            //get phi for current variable
            List<Phi> phiInstructions = phiStack.peek();

            List<Phi> phisForV = phiInstructions.stream()
                    .filter(phi -> phi.old != null && phi.old.getId() == v.getId())
                    .collect(Collectors.toList());
            assert phisForV.size() <= 1;

            Phi phiForV;
            if (phisForV.isEmpty()) {  // no phi found. add phi to be added to join later.
                phiForV = new Phi();
                phiForV.old = currentBlock.getOldAssignmentFromParent(v);
                phiInstructions.add(phiForV);
            } else {
                phiForV = phisForV.get(0);
            }

            if (leftBranch) {
                phiForV.left = v.copy();
            } else {
                phiForV.right = v.copy();
            }
        }

        // Add value instance to var-instance map
        varIDInstanceMap.put(v.getId(), v);
    }

    public Variable getCurrentValueInstance(String varName, BasicBlock currentBlock, boolean isDef) {
        Variable currentVar = Objects.requireNonNull(ScopeManager.INSTANCE.findTokenInScope(varName)).copy();
        currentVar = currentBlock.getAssignment(currentVar);

        if (!varDefUseChain.containsKey(currentVar)) {
            varDefUseChain.put(currentVar.copy(), new ArrayList<>());
        }
        if (!isDef) { // add use to def-use chain
            varDefUseChain.get(currentVar).add(currentVar);
        }

        return currentVar;
    }

    public void pushToPhiStack() {
        phiStack.push(new ArrayList<>());
    }

    public void addPhiInstructionsForIf(Converter converter) {
        List<Phi> phiInstructions = phiStack.pop();

        for (Phi phi : phiInstructions) {
            if (phi.left == null) {
                phi.left = phi.old.copy();
            } else if (phi.right == null) {
                phi.right = phi.old.copy();
            }
            Variable newVar = phi.old.copy();
            newVar.assignmentLocation = converter.phi(newVar, phi.left, phi.right);

            // add the left and right phi to use chains
            addPhiToUseChain(phi.left, newVar);
            addPhiToUseChain(phi.right, newVar);

            // create value instance and use chain for new var
            addValueInstance(newVar, converter.getCurrentBlock());
        }
    }

    private void addPhiToUseChain(Variable phiVar, Variable newVar) {
        phiVar.useLocation = newVar.assignmentLocation.number;
        if (varDefUseChain.containsKey(phiVar))
            varDefUseChain.get(phiVar).add(phiVar);
    }

    public void addPhiForWhile(Converter converter, BasicBlock joinBlock) {
        converter.setCurrentBlock(joinBlock);
        int resetLocation = joinBlock.getInstructionList().get(0).number;
        List<Phi> phiInstructions = phiStack.pop();
        for (Phi phi : phiInstructions) {
            Variable newVar = phi.old.copy();
            List<Variable> useChain = varDefUseChain.get(newVar);
            newVar.assignmentLocation = converter.whilePhi(newVar, phi.old, phi.right);

            // add the left and right phi to use chains
            addPhiToUseChain(phi.old, newVar);
            addPhiToUseChain(phi.right, newVar);

            // create value instance and use chain for new var
            addValueInstance(newVar, converter.getCurrentBlock());

            // update uses with new value instance
            List<Variable> newUseChain = varDefUseChain.get(newVar);
            newUseChain.addAll(updateUses(newVar, useChain, resetLocation));
        }
    }

    private List<Variable> updateUses(Variable newVar, List<Variable> useChain, int resetLocation) {
        List<Variable> newUseList = new ArrayList<>();
        for (int i = 0; i < useChain.size(); i++) {
            Variable use = useChain.get(i);
            if (use.useLocation >= resetLocation && // change only uses after phi
                    use.useLocation != newVar.assignmentLocation.number) { // don't change the phi itself
                useChain.remove(i);
                use.assignmentLocation = newVar.assignmentLocation;
                newUseList.add(use);
                i--; // removed element
            }
        }
        return newUseList;
    }
}
