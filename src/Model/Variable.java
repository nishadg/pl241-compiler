package Model;

import IR.CFG;
import IR.ScopeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Variable extends Result {
    int id;
    String name;

    public List<Result> indices;
    public List<Integer> dimensions;
    public Instruction assignmentLocation;
    public int useLocation;
    public boolean isFunction;
    private List<Result> parameters;

    public Instruction getValueLocation() {
        return valueLocation == null? CFG.INSTANCE.getFirstInstruction() : valueLocation;
    }



    private Instruction valueLocation;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private Variable(int id, String name) {
        super(Kind.VAR);
        this.id = id;
        this.name = name;
        indices = new ArrayList<>();
        dimensions = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder indexString = new StringBuilder();
//        for (Result i : indices) {
//            indexString.append("[").append(i).append("]");
//        }
        if (!isFunction) {
            indexString.append("_");
            if (assignmentLocation == null) {
                indexString.append("?");
            } else {
                indexString.append(assignmentLocation.number);
            }
            indexString.append(valueLocation);
        }else{
            indexString.append(parameters);
        }
        return name + indexString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable v = (Variable) obj;
            return !this.isFunction && !v.isFunction && // function calls cannot be compared
                    this.id == v.id && this.assignmentLocation == v.assignmentLocation;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, assignmentLocation);
    }

    public Variable copy() {
        Variable v = new Variable(this.getId(), this.name);
        v.dimensions = new ArrayList<>(this.dimensions);
        v.assignmentLocation = this.assignmentLocation;
        v.valueLocation = this.valueLocation;
        return v;
    }

    public static Variable create(int id, String name) {
        Variable v = new Variable(id, name);
        v.dimensions = new ArrayList<>();
        return v;
    }

    public boolean isGlobal() {
        return ScopeManager.INSTANCE.isVarGlobal(this);
    }

    public void setValueLocation(Instruction valueLocation) {
        this.valueLocation = valueLocation;
    }

    public void addParams(List<Result> params) {
        assert isFunction;

        this.parameters = params;
    }
}
