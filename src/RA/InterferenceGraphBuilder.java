package RA;

import IR.CFG;
import Model.*;
import test.DotGenerator;

import java.io.IOException;
import java.util.*;

public class InterferenceGraphBuilder {
    HashMap<Integer, Set<Integer>> interferenceGraph = new HashMap<>();
    private String name;

    public InterferenceGraphBuilder(String name) {
        this.name = name;
    }


    public void buildInterferenceGraph(List<List<BasicBlock>> CFG) {
        for (List<BasicBlock> functionBlockList : CFG) {
            addToGraph(functionBlockList.get(functionBlockList.size() - 1), new HashSet<>());
        }
        createDotFile();
    }

    private void createDotFile() {
        try {
            DotGenerator dotGenerator = new DotGenerator(name);
            dotGenerator.generateInterference(interferenceGraph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addToGraph(BasicBlock lastBlock, Set<Integer> liveValues) {
        // create left and right live values
        Set<Integer> leftLive = new HashSet<>();
        Set<Integer> rightLive = new HashSet<>();

        // combine live values for if block
        if (lastBlock.leftLiveValues != null) liveValues.addAll(lastBlock.leftLiveValues);

        // parse instructions and add/remove from live values
        List<Instruction> instructionList = lastBlock.getInstructionList();
        for (int i = instructionList.size() - 1; i >= 0; i--) {
            Instruction currentInstruction = instructionList.get(i);
            if (currentInstruction.isDeleted()) continue;

            removeFromLive(currentInstruction, liveValues);
            if (currentInstruction.isPhiInstruction) {
                addToLive(currentInstruction.getX(), leftLive);
                addToLive(currentInstruction.getY(), rightLive);
            } else {
                addToLive(currentInstruction.getX(), liveValues);
                addToLive(currentInstruction.getY(), liveValues);
            }
        }

        // recursively call parents
        if (lastBlock.parents.size() > 0) {
            BasicBlock parent = lastBlock.parents.get(0);
            if (parent.isIfParent && !parent.leftTreeParsed) { // if coming from left block store live values in block
                parent.leftTreeParsed = true;
                parent.leftLiveValues = new HashSet<>(liveValues);
            } else {
                leftLive.addAll(liveValues);
                addToGraph(parent, leftLive);
            }

            // parse right parent branch
            if (lastBlock.parents.size() > 1) {
                rightLive.addAll(liveValues);
                addToGraph(lastBlock.parents.get(1), rightLive);
            }
        }
    }

    private void removeFromLive(Instruction currentInstruction, Set<Integer> liveValues) {
        if (liveValues.contains(currentInstruction.number))
            liveValues.remove(currentInstruction.number);
        else
            currentInstruction.kill();
    }

    private void addToLive(Result x, Set<Integer> liveValues) {
        if (x != null) {
            int n;
            if (x.kind == Kind.VAR) {
                n = ((Variable) x).valueLocation.number;
            } else if (x.kind == Kind.ADDR) {
                if (x instanceof BasicBlock) return; // TODO: handle compare statements where argument is a basic block
                n = ((Instruction) x).getValueLocation().number;
            } else {
                return;
            }
            for (int i : liveValues) {
                if (i != n) {
                    if (interferenceGraph.containsKey(i)){
                        interferenceGraph.get(i).add(n);
                    } else if (interferenceGraph.containsKey(n)){
                        interferenceGraph.get(n).add(i);
                    }else{
                        Set<Integer> newNode = new HashSet<>();
                        interferenceGraph.put(n, newNode);
                        newNode.add(i);
                    }
                }
            }

            liveValues.add(n);
        }
    }

    public void allocate() {
        buildInterferenceGraph(CFG.INSTANCE.getGraphs());
    }
}
