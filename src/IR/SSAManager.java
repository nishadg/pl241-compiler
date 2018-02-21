package IR;

import Model.Variable;
import Parser.RParser;

import java.util.HashMap;
import java.util.Objects;

public enum SSAManager {
    INSTANCE;


    HashMap<Integer, Variable> varIDInstanceMap = new HashMap<>();

    public void addValueInstance(Variable v) {
        varIDInstanceMap.put(v.getId(), v);
    }

    public Variable getCurrentValueInstance(String varName) {
        Variable v = Objects.requireNonNull(ScopeManager.INSTANCE.findTokenInScope(varName)).copy();
        if (varIDInstanceMap.containsKey(v.getId())) {
            v = varIDInstanceMap.get(v.getId()).copy();
        }
        return v;
    }
}
