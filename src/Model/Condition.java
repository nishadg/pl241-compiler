package Model;

public class Condition extends Result{
    public int operator;

    public Condition(int operator) {
        super(Kind.COND);
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Should you be printing this?";
    }
}
