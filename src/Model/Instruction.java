package Model;

import static Model.Instruction.DeleteReason.DCE;

public class Instruction extends Result {
    public Instruction getValueLocation() {
        return isDeleted && deletedBecause != DCE ? replacementInstruction.getValueLocation() : this;
    }

    public enum DeleteReason {
        CSE, CP, DCE
    }

    DeleteReason deletedBecause;

    public boolean isDeleted() {
        return isDeleted;
    }

    boolean isDeleted;
    Instruction replacementInstruction;

    public static void resetCounter() {
        counter = 0;
    }

    private static int counter = 0;

    public Operation getOp() {
        return op;
    }

    Operation op;

    Result x;
    Result y;

    public void setX(Result x) {
        this.x = x;
    }

    public void setY(BasicBlock y) {
        this.y = y;
    }

    public Result getX() {
        return x;
    }

    public Result getY() {
        return y;
    }


    public int number;
    public boolean isPhiInstruction;
    public Variable phiVar;
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
            return "(" + getValueLocation().number + ")";
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

    public boolean containsGlobals() {
        return (x != null && x.kind == Kind.VAR && ((Variable) x).isGlobal())
                || (y != null && y.kind == Kind.VAR && ((Variable) y).isGlobal());
    }

    public String outputString() {
        if (isDeleted) {
            if (replacementInstruction != null) {
                return number + ": " + replacementInstruction.toString() + " - " + deletedBecause;
            } else {
                return number + ": " + deletedBecause;
            }
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

    public void propagateCopy(Result x) {
        isDeleted = true;
        deletedBecause = DeleteReason.CP;
        replacementInstruction = (Instruction) x;
    }

    public void kill() {
        switch (op) {
            case write:
            case bra:
            case ble:
            case bge:
            case bgt:
            case blt:
            case bne:
            case beq:
            case writeNL:
            case end:
            case call:
                // do nothing
                break;
            default:
                isDeleted = true;
                deletedBecause = DCE;
        }
    }

}
