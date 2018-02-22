package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Variable extends Result {
    int id;
    String name;

    public List<Result> indices;
    public List<Integer> dimensions;
    public Value assignmentLocation;

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
        for (Result i : indices) {
            indexString.append("[").append(i).append("]");
        }
        indexString.append("_").append(assignmentLocation.location);
        return name + indexString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable v = (Variable) obj;
            return this.id == v.id && this.assignmentLocation == v.assignmentLocation;
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
        return v;
    }

    public static Variable create(int id, String name){
        Variable v = new Variable(id, name);
        v.dimensions = new ArrayList<>();
        return v;
    }
}
