package IR;

import Model.Variable;

import java.util.*;
import java.util.stream.Collectors;

public enum ScopeManager {
    INSTANCE;

    public static final String MAIN_SCOPE = "MAIN";
    HashMap<String, List<Variable>> scopeVarMap = new HashMap<>();
    private String currentScope;

    public void addVarToScope(Variable v) {
        addVarToScope(v, currentScope);
    }

    public void addVarToScope(Variable v, String functionName) {
        List<Variable> varList = scopeVarMap.get(functionName);
        varList.add(v);
    }

    public void createScope(String functionName) {
        scopeVarMap.put(functionName, new ArrayList<>());
        currentScope = functionName;
    }

    public Variable findTokenInScope(String currentToken) {
        List<Variable> varList = scopeVarMap.get(currentScope);
        List<Variable> foundVars = varList.stream()
                .filter(v -> Objects.equals(v.getName(), currentToken))
                .collect(Collectors.toList());

        if (foundVars.size() == 0) {
            varList = scopeVarMap.get(MAIN_SCOPE);
            foundVars = varList.stream()
                    .filter(v -> Objects.equals(v.getName(), currentToken))
                    .collect(Collectors.toList());
        }
        return (foundVars.size() == 0) ? null : foundVars.get(0);
    }

    public int getID(String varName) {
        Variable v = findTokenInScope(varName);
        assert v != null;
        return v.getId();
    }

    public void createVar(int id, String name) {
        Variable v = new Variable(id, name);
        addVarToScope(v);
    }

    public void backToMain() {
        currentScope = MAIN_SCOPE;
    }
}
