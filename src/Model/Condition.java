package Model;

public class Condition extends Result{
    public int operator;
    public Instruction compareLocation;

    public Condition(int operator, Instruction compareLocation) {
        super(Kind.COND);
        this.operator = operator;
        this.compareLocation = compareLocation;
    }

    @Override
    public String toString() {
        return "Should you be printing this?";
    }

}
