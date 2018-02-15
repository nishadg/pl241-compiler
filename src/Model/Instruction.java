package Model;

public class Instruction {
    public static int getCounter() {
        return counter;
    }

    private static int counter = 0;
    Operation op;
    Result x;
    Result y;

    public int getNumber() {
        return number;
    }

    int number;

    public Instruction(Operation op, Result x, Result y) {
        this.op = op;
        this.x = new Result(x);
        this.y = new Result(y);
        number = counter++;
    }

    public Instruction(Operation op, Result x) {
        this.op = op;
        this.x = new Result(x);
        number = counter++;
    }

    public Instruction(Operation op) {
        this.op = op;
        number = counter++;
    }
}
