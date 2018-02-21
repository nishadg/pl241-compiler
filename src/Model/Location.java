package Model;

import IR.Converter;

public class Location extends Result{
    public int location;

    public Location(int location) {
        super(Kind.ADDR);
        this.location = location;
    }

    @Override
    public String toString() {
        return "(" + location + ")";
    }
}
