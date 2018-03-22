package Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Operation {
    neg, add, sub, mul, div, cmp, adda, load, store, move, phi, end, bra, bne, beq, ble, blt, bge, bgt, read, write, call, ret, init, writeNL;

    public static Set<Operation> nonAnchored = Set.of(cmp, bra, bne, beq, ble, blt, bge, bgt, read, write, writeNL, ret, init, end);
}