package Model;

import Parser.Token;

public class Constant extends Result {
    int value;

    public Constant(int value) {
        super(Kind.CONST);
        this.value = value;
    }

    public Result compute(int op, Result y) {
        if (y.kind == Kind.CONST) {
            Constant c = (Constant) y;
            switch (op) {
                case Token.plusToken:
                    value += c.value;
                    break;
                case Token.minusToken:
                    value -= c.value;
                    break;
                case Token.timesToken:
                    value *= c.value;
                    break;
                case Token.divToken:
                    value /= c.value;
                    break;
                default:
                    System.out.print("Invalid operation");
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
