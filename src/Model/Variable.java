package Model;

import java.util.Comparator;

public class Variable extends Result {
    public int getId() {
        return id;
    }

    int id;

    public String getName() {
        return name;
    }

    String name;

    public Variable(int id, String name) {
        super(Kind.VAR);
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Variable) {
            Variable v = (Variable) obj;
            return this.id == v.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public static class NameComparator implements Comparator<Variable>{
        @Override
        public int compare(Variable v1, Variable v2) {
            return v1.name.compareTo(v2.name);
        }
    }
}
