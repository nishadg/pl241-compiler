package Model;

public class Address extends Result{
    public int location;

    public Address(int location) {
        super(Kind.ADDR);
        this.location = location;
    }

    @Override
    public String toString() {
        return "(" + location + ")";
    }
}
