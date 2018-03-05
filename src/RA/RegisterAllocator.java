package RA;

import IR.CFG;
import Model.*;

import java.util.*;

public class RegisterAllocator {
    HashMap<Integer, List<Integer>> interferenceGraph = new HashMap<>();
    Set<Integer> liveValues = new HashSet<>();

    public void buildInterferenceGraph(List<List<BasicBlock>> CFG) {
        for (List<BasicBlock> functionBlocks : CFG) {
            addToGraph(functionBlocks.get(functionBlocks.size() - 1));
        }
    }

    void addToGraph(BasicBlock lastBlock) {
        List<Instruction> instructionList = lastBlock.getInstructionList();
        for (int i = instructionList.size() - 1; i >= 0; i--) {
            Instruction currentInstruction = instructionList.get(i);
            if (currentInstruction.isDeleted()) continue;
            removeFromLive(currentInstruction);
            addToLive(currentInstruction.getX());
            addToLive(currentInstruction.getY());
        }
    }

    private void removeFromLive(Instruction currentInstruction) {
        if (liveValues.contains(currentInstruction.number))
            liveValues.remove(currentInstruction.number);
        else
            currentInstruction.kill();
    }

    private void addToLive(Result x) {
        if (x != null) {
            int n;
            if (x.kind == Kind.VAR) {
                n = ((Variable) x).valueLocation.number;
            } else if (x.kind == Kind.ADDR) { // TODO: handle compare statements where argument is a basic block
                n = ((Instruction) x).getValueLocation().number;
            } else {
                return;
            }
            for (int i : liveValues) {
                if (i != n)
                    interferenceGraph.get(i).add(n);
            }
            if (!liveValues.contains(n)) {
                interferenceGraph.put(n, new ArrayList<>());
                liveValues.add(n);
            }
        }
    }

    public void allocate() {
        buildInterferenceGraph(CFG.INSTANCE.getGraphs());
    }
}
