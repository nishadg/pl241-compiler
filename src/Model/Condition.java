package Model;

public class Condition extends Result{
    public int operator;
    public Location compareLocation;

    public Condition(int operator, Location compareLocation) {
        super(Kind.COND);
        this.operator = operator;
        this.compareLocation = compareLocation;
    }

    @Override
    public String toString() {
        return "Should you be printing this?";
    }

}
