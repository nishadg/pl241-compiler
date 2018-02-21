package Model;

import IR.Converter;

public class Value extends Result{
    public int location;

    public Value(int location) {
        super(Kind.ADDR);
        this.location = location;
    }

    @Override
    public String toString() {
        return "(" + location + ")";
    }
}
