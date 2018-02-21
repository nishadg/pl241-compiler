package IR;

import Model.Variable;

import java.util.*;
import java.util.stream.Collectors;

public enum SSAManager {
    INSTANCE;

    class Phi {
        Variable old;
        Variable left;
        Variable right;
    }

    public boolean leftBranch = true;
    HashMap<Integer, Variable> varIDInstanceMap = new HashMap<>();
    Stack<List<Phi>> phiStack = new Stack<>();

    public void addValueInstance(Variable v) {
        Phi p = new Phi();
        p.old = varIDInstanceMap.getOrDefault(v.getId(), null);
        varIDInstanceMap.put(v.getId(), v);
        if (!phiStack.empty()) { // we are in one of the branches
            //get phi for current variable
            List<Phi> phiInstructions = phiStack.peek();
            List<Phi> phisForV = phiInstructions.stream().filter(phi -> phi.old.getId() == v.getId()).collect(Collectors.toList());
            assert phisForV.size() <= 1;
            Phi phiForV;
            if (phisForV.isEmpty()) {  // no phi found. add phi to be added to join later.
                phiForV = p;
                phiInstructions.add(phiForV);
            } else {
                phiForV = phisForV.get(0);
            }
            if (leftBranch) {
                phiForV.left = v;
            } else {
                phiForV.right = v;
            }
        }
    }

    public Variable getCurrentValueInstance(String varName) {
        Variable v = Objects.requireNonNull(ScopeManager.INSTANCE.findTokenInScope(varName)).copy();
        if (varIDInstanceMap.containsKey(v.getId())) {
            v = varIDInstanceMap.get(v.getId()).copy();
        }
        return v;
    }

    public void pushToPhiStack() {
        phiStack.push(new ArrayList<>());
    }

    public void addPhiInstructionsToCurrentBlock() {
        List<Phi> phiInstructions = phiStack.pop();
        for (Phi phi : phiInstructions) {
            if (phi.left == null) {
                phi.left = phi.old;
            } else if (phi.right == null) {
                phi.right = phi.old;
            }
            Variable newVar = phi.old.copy();
            newVar.assignmentLocation = Converter.INSTANCE.phi(newVar, phi.left, phi.right);
            SSAManager.INSTANCE.addValueInstance(newVar);
        }
    }
}
