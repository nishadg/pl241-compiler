package Machine;

import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Model.Operation.*;

public class MachineCodeGenerator {


    HashMap<Operation, Integer> opCodes = new HashMap<>(Map.of(
            add, DLX.ADD,
            sub, DLX.SUB,
            mul, DLX.MUL,
            div, DLX.DIV,
            ret, DLX.RET,
            end, DLX.RET,
            read, DLX.RDI,
            write, DLX.WRD,
            writeNL, DLX.WRL
    ));

    private int programCounter = 0;
    private ArrayList<Integer> program = new ArrayList<>();
    private HashMap<Integer, Integer> instructionRegisters;

    public MachineCodeGenerator(HashMap<Integer, Integer> instructionRegisters) {

        this.instructionRegisters = instructionRegisters;
    }


    public int[] generate(List<List<BasicBlock>> functions) {
        for (List<BasicBlock> function : functions) {
            for (BasicBlock block : function) {
                for (Instruction instruction : block.getInstructionList()) {
                    if (!instruction.isDeleted() && instruction.getOp() != init)
                        convertToDLX(instruction);
                }
            }
        }
        int[] programArray = new int[program.size()];
        for (int i = 0; i < program.size(); i++){
            programArray[i] = program.get(i);
            System.out.print(DLX.disassemble(programArray[i]));
        }
        return programArray;
    }

    private void convertToDLX(Instruction instruction) {
        assert !instruction.isDeleted();
        Operation op = instruction.getOp();
        int machineOpCode;
        int Ra, Rb, C;
        switch (op) {
            case add:
            case sub:
            case mul:
            case div:
                machineOpCode = opCodes.get(op);
//                assert instruction.getX() instanceof Instruction;
                Ra = getRegister(instruction);
                Rb = getRegister(instruction.getX());
                if (instruction.getY().kind == Kind.CONST) {
                    machineOpCode += 16;
//                    assert instruction.getY() instanceof Constant;
                    C = ((Constant) instruction.getY()).getValue();
                } else {
//                    assert instruction.getY() instanceof Instruction;
                    C = getRegister(instruction.getY());
                }
                addOperation(machineOpCode, Ra, Rb, C);
                break;
            case write:
                Rb = getRegister(instruction.getX());
                addOperation(opCodes.get(op), 0, Rb, 0);
                break;
            case writeNL:
            case end:
                addOperation(opCodes.get(op), 0, 0, 0);
                break;

        }

    }

    private int getRegister(Result x) {
        if (x.kind == Kind.REG) {
//                    assert instruction.getX() instanceof Register;
            return ((Register) x).regNo;
        } else {
            Instruction value;
            if (x.kind == Kind.ADDR)
                value = ((Instruction) x).getValueLocation();
            else
                value = ((Variable) x).getValueLocation();
            return instructionRegisters.get(value.number) + 1;
        }
    }

    private void addOperation(int machineOpCode, int ra, int rb, int c) {
        program.add(programCounter++, machineOpCode << 26 | ra << 21 | rb << 16 | c & 0xffff);
    }
}
