package Model;

public abstract class Result {
    public Kind kind; // const, var, reg, condition

    public Result(Kind kind) {
        this.kind = kind;
    }
}

