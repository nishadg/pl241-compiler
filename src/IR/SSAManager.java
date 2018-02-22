package IR;

import Model.BasicBlock;
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

        if (!phiStack.empty()) { // we are in one of the branches
            //store old value instance in phi for later use.
            Phi p = new Phi();
            p.old = currentBlock.getOldAssignmentFromParent(v);
            if (p.old == null) p.old = v.copy();

            //get phi for current variable
            List<Phi> phiInstructions = phiStack.peek();

            List<Phi> phisForV = phiInstructions.stream()
                    .filter(phi -> phi.old != null && phi.old.getId() == v.getId())
                    .collect(Collectors.toList());
            assert phisForV.size() <= 1;
            Phi phiForV;
            if (phisForV.isEmpty()) {  // no phi found. add phi to be added to join later.
                phiForV = p;
                if (phiForV.old == null) phiForV.old = v.copy();
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

    public Variable getCurrentValueInstance(String varName, boolean isDef) {
        Variable v = Objects.requireNonNull(ScopeManager.INSTANCE.findTokenInScope(varName)).copy();
        if (varIDInstanceMap.containsKey(v.getId())) {
            v = varIDInstanceMap.get(v.getId()).copy();

            // add use to def-use chain
            if (!isDef) {
                varDefUseChain.get(v).add(v);
            }
        }
        return v;
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
            addValueInstance(newVar, converter.getCurrentBlock());
        }
    }

    public void addPhiForWhile(Converter converter, BasicBlock joinBlock) {
        converter.setCurrentBlock(joinBlock);
        int resetLocation = joinBlock.getInstructionList().get(0).number;
        List<Phi> phiInstructions = phiStack.pop();
        for (Phi phi : phiInstructions) {
            Variable newVar = phi.old.copy();
            List<Variable> useChain = varDefUseChain.get(newVar);
            newVar.assignmentLocation = converter.whilePhi(newVar, phi.old, phi.right);
            addValueInstance(newVar, converter.getCurrentBlock());
            for (Variable use : useChain) {
                if (use.useLocation != null && use.useLocation.number >= resetLocation)
                    use.assignmentLocation = newVar.assignmentLocation;
            }
        }
    }
}
