package Model;

import java.util.Set;

public enum Operation {
    neg, add, sub, mul, div, cmp, adda, load, store, move, phi, end, bra, bne, beq, ble, blt, bge, bgt, read, write, addi, subi, divi, muli, call, ret, init, writeNL;

    public static Set<Operation> nonAnchored = Set.of(cmp, bra, bne, beq, ble, blt, bge, bgt, call, read, write, writeNL, ret, init, end, phi);
}