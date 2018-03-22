package Machine;

import Model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Model.Operation.*;

public class MachineCodeGenerator {

    HashMap<Operation, Integer> opCodes = new HashMap<>(Map.ofEntries(
            Map.entry(add, DLX.ADD),
            Map.entry(sub, DLX.SUB),
            Map.entry(mul, DLX.MUL),
            Map.entry(div, DLX.DIV),
            Map.entry(cmp, DLX.CMP),
            Map.entry(beq, DLX.BEQ),
            Map.entry(bne, DLX.BNE),
            Map.entry(blt, DLX.BLT),
            Map.entry(bge, DLX.BGE),
            Map.entry(ble, DLX.BLE),
            Map.entry(bgt, DLX.BGT),
            Map.entry(bra, DLX.BSR),
            Map.entry(ret, DLX.RET),
            Map.entry(end, DLX.RET),
            Map.entry(read, DLX.RDI),
            Map.entry(write, DLX.WRD),
            Map.entry(writeNL, DLX.WRL)
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
                block.programCounter = programCounter;
                if (block.areBranchUpdatesPending()) {
                    for (BasicBlock b : block.pendingBranchUpdates) {
                        addCForCode(programCounter, b);
                    }
                    block.pendingBranchUpdates.clear();
                }
                for (Instruction instruction : block.getInstructionList()) {
                    if (!instruction.isDeleted() && instruction.getOp() != init)
                        convertToDLX(block, instruction);
                }
            }
        }
        int[] programArray = new int[program.size()];
        for (int i = 0; i < program.size(); i++) {
            programArray[i] = program.get(i);
            System.out.print((i+1) + " " + DLX.disassemble(programArray[i]));
        }
        return programArray;
    }

    private void addCForCode(int currentPC, BasicBlock branchToBlock) {
        int branchPCAddress = branchToBlock.branchPCAddress;
        int code = program.remove(branchPCAddress) & 0xffff0000;
        code += (currentPC - branchPCAddress);
        program.add(branchPCAddress, code);
    }

    private void convertToDLX(BasicBlock block, Instruction instruction) {
        assert !instruction.isDeleted();
        Operation op = instruction.getOp();
        int machineOpCode;
        int Ra = 0, Rb, C = 0;
        switch (op) {
            case add:
            case sub:
            case mul:
            case div:
            case cmp:
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
            case beq:
            case bne:
            case blt:
            case bge:
            case ble:
            case bgt:
                Ra = getRegister(instruction.getX());
                BasicBlock branchToBlock = (BasicBlock) instruction.getY();
                branchToBlock.pendingBranchUpdates.add(block);
                block.branchPCAddress = programCounter;
                addOperation(opCodes.get(op), Ra, 0, 0);
                break;
            case bra:
                BasicBlock bsrToBlock = (BasicBlock) instruction.getX();
                if (bsrToBlock.isIfJoin) {
                    bsrToBlock.pendingBranchUpdates.add(block);
                    block.branchPCAddress = programCounter;
                } else if (bsrToBlock.isWhileJoin) {
                    C = bsrToBlock.programCounter - programCounter;
                }
                addOperation(opCodes.get(op), 0, 0, C);
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
            if (value.number == 0) return 0;
            return instructionRegisters.get(value.number) + 1;
        }
    }

    private void addOperation(int machineOpCode, int ra, int rb, int c) {
        program.add(programCounter++, machineOpCode << 26 | ra << 21 | rb << 16 | c & 0xffff);
    }
}
