package Model;

public class Result {
    public KIND kind; // const, var, reg, condition
    public int value; // value if it is a constant
    int address; // address if it is a variable
    public int regNo; // register number if it is a reg or a condition
    int cond, fixuplocation; // if it is a condition

    public Result(KIND kind, int value) {
        this.kind = kind;
        switch (kind){
            case VAR:
                address = value;
                break;
            case CONST:
                this.value = value;
                break;
            case REG:
                this.regNo = value;
        }
    }
}

