package Model;

import IR.CFG;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    static int counter = 0;

    public int index;
    List<Instruction> instructionList = new ArrayList<>();
    public List<BasicBlock> parents = new ArrayList<>();
    public BasicBlock leftBlock;
    public BasicBlock rightBlock;
    public boolean isJoin = false;

    private BasicBlock() {
        index = counter++;
    }

    public static BasicBlock create(){
        BasicBlock b = new BasicBlock();
        CFG.INSTANCE.currentGraph.add(b);
        return b;
    }
}
