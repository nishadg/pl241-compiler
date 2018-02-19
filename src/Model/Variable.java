package Model;

public class Variable extends Result {
    int id;
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
}
