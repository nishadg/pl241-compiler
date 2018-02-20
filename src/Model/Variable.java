package Model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Variable extends Result {
    int id;
    String name;

    public List<Result> indices;
    public List<Integer> dimensions;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Variable(int id, String name) {
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
        return name + indexString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            Variable v = (Variable) obj;
            return this.id == v.id;
        }
        return false;
    }

    public Variable copy() {
        Variable v = new Variable(this.getId(), this.name);
        v.dimensions = new ArrayList<>(this.dimensions);
        return v;
    }
}
