package Model;

import IR.Converter;

public class Instruction extends Result {
    public enum DeleteReason {
        CSE, CP
    }

    DeleteReason deletedBecause;
    boolean isDeleted;
    Instruction replacementInstruction;

    public static void resetCounter() {
        counter = 0;
    }

    private static int counter = 0;
    Operation op;

    public void setX(Result x) {
        this.x = x;
    }

    Result x;
    Result y;

    public int number;
    boolean isPhiInstruction;
    Variable phiVar;
    BasicBlock containingBlock;

    public void setPhiInstruction(Variable phiVar) {
        isPhiInstruction = true;
        this.phiVar = phiVar;
    }

    public Instruction(Operation op) {
        this(op, null, null);
    }

    public Instruction(Operation op, Result x, Result y) {
        super(Kind.ADDR);
        this.op = op;
        this.x = x;
        this.y = y;
        number = counter++;

        // set use instruction for variable
        if (x != null && x.kind == Kind.VAR) {
            ((Variable) x).useLocation = number;
        }
        if (y != null && y.kind == Kind.VAR) {
            ((Variable) y).useLocation = number;
        }
    }

    public Instruction(Operation op, Result x) {
        this(op, x, null);
    }

    @Override
    public String toString() {
        if (isDeleted) {
            Instruction originalValue = this;
            while (originalValue.isDeleted) {
                originalValue = originalValue.replacementInstruction;
            }
            return  "(" + originalValue.number + ")";
        } else
            return "(" + number + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Instruction) {
            Instruction i = (Instruction) obj;
            if (i.op == this.op) {
                return areOperandsSame(i.x, i.y)
                        || (i.op == Operation.mul || i.op == Operation.add) && areOperandsSame(i.y, i.x);
            }
        }
        return false;
    }

    private boolean areOperandsSame(Result x, Result y) {
        boolean xIsSame = (this.x == null && x == null) || this.x.equals(x);
        boolean yIsSame = (this.y == null && y == null) || this.y.equals(y);
        return xIsSame && yIsSame;
    }

    public String outputString() {
        if (isDeleted) {
            return number + ": " + replacementInstruction.toString() + " - " + deletedBecause;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(number).append(": ");
        stringBuilder.append(op.name());
        if (isPhiInstruction) {
            stringBuilder.append(" ").append(phiVar).append(" := ");
        }
        if (x != null) stringBuilder.append(" ").append(x);
        if (y != null) stringBuilder.append(" ").append(y);
        return stringBuilder.toString();
    }

    public void setY(BasicBlock y) {
        this.y = y;
    }

    public void propagateCopy(Result x) {
        isDeleted = true;
        deletedBecause = DeleteReason.CP;
        replacementInstruction = (Instruction) x;
    }
}
