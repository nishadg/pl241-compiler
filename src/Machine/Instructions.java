package Machine;

import Model.Instruction;
import Model.KIND;
import Model.OPERATION;
import Model.Result;
import Parser.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public enum Instructions {
    INSTANCE;

    public ArrayList<Instruction> instructions = new ArrayList<>();

    public void compute(int op, Result x, Result y) {
        if (x.kind == KIND.CONST && y.kind == KIND.CONST) {
            switch (op) {
                case Token.plusToken:
                    x.value += y.value;
                    break;
                case Token.minusToken:
                    x.value -= y.value;
                    break;
                case Token.timesToken:
                    x.value *= y.value;
                    break;
                case Token.divToken:
                    x.value /= y.value;
                    break;
                default:
                    System.out.print("Invalid operation");
            }
        } else {
            load(x);
            if (x.regNo == 0) {
                x.regNo = Register.allocate();
//                PutF1(ADD, x.regno, 0, 0);
            }
            if (y.kind == KIND.CONST) {
//                PutF1(opCodeImm[op], x.regno, x.regno, y.value);
            } else {
                load(y);
//                PutF1(opCode[op], x.regno, x.regno, y.regno);
                Register.deallocate(y.regNo);
            }
        }
    }

    private void load(Result x) {
        if (x.kind == KIND.VAR) {
            x.regNo = Register.allocate();
//            PutF1(LDW, x.regno, basereg, x.address);
            x.kind = KIND.REG;
        } else if (x.kind == KIND.CONST) {
            if (x.value == 0) x.regNo = 0;
            else {
                x.regNo = Register.allocate();
//                PutF1(ADDI, x.regno, 0, x.value);
            }
            x.kind = KIND.REG;
        }
    }
}
