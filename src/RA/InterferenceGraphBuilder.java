package RA;

import IR.CFG;
import Model.*;
import test.DotGenerator;

import java.io.IOException;
import java.util.*;

public class InterferenceGraphBuilder {
    HashMap<Integer, Set<Integer>> interferenceGraph = new HashMap<>();
    HashMap<Integer, Integer> instructionColors;
    HashMap<Integer, Set<Integer>> phiCluster = new HashMap<>();
    private String name;

    public InterferenceGraphBuilder(String name) {
        this.name = name;
    }


    public HashMap<Integer, Integer> buildInterferenceGraph(List<List<BasicBlock>> CFG) {
        for (List<BasicBlock> functionBlockList : CFG) {
            addToGraph(functionBlockList.get(functionBlockList.size() - 1), new HashSet<>());
        }
        instructionColors = new Allocator(interferenceGraph, phiCluster).colorGraph();
        createDotFile();
        return instructionColors;
    }

    private void createDotFile() {
        try {
            DotGenerator dotGenerator = new DotGenerator(name);
            dotGenerator.generateInterference(interferenceGraph, instructionColors);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addToGraph(BasicBlock lastBlock, Set<Integer> liveValues) {
        // create left and right live values
        Set<Integer> leftLive = new HashSet<>();
        Set<Integer> rightLive = new HashSet<>();
        Set<Integer> phiLive = new HashSet<>();

        // combine live values for if block
        if (lastBlock.otherSubtreeLiveValues != null) liveValues.addAll(lastBlock.otherSubtreeLiveValues);

        // parse instructions and add/remove from live values
        List<Instruction> instructionList = lastBlock.getInstructionList();
        for (int i = instructionList.size() - 1; i >= 0; i--) {
            Instruction currentInstruction = instructionList.get(i);

            if (currentInstruction.number == 0) continue;
            if (currentInstruction.isDeleted()) continue;

            if (currentInstruction.isPhiInstruction) {
                addToPhiCluster(currentInstruction);
                boolean isKilled = false;
                if ((lastBlock.isWhileJoin && lastBlock.otherSubtreeParsed) || lastBlock.isIfJoin) {
                    isKilled = removeFromLive(currentInstruction, liveValues);
                } else {
                    addToLive(currentInstruction, phiLive);
                }
                if (!isKilled) {
                    addToLive(currentInstruction.getX(), leftLive);
                    addToLive(currentInstruction.getY(), rightLive);
                }
            } else {
                removeFromLive(currentInstruction, liveValues);
                addToLive(currentInstruction.getX(), liveValues);
                addToLive(currentInstruction.getY(), liveValues);
            }
        }


        if (lastBlock.parents.isEmpty()) return; // finish on first block

        if (lastBlock.isWhileJoin && !lastBlock.otherSubtreeParsed) { // send right and phi values to loop
            lastBlock.otherSubtreeParsed = true;
            rightLive.addAll(liveValues);
            rightLive.addAll(phiLive);
            addToGraph(lastBlock.parents.get(1), rightLive);
            return;
        }

        // parse parent
        BasicBlock leftParent = lastBlock.parents.get(0);
        if (leftParent.isIfParent && !leftParent.otherSubtreeParsed) { // if coming from left block store live values in block
            leftParent.otherSubtreeParsed = true;
            leftParent.otherSubtreeLiveValues = new HashSet<>(liveValues);
        } else {
            leftLive.addAll(liveValues);
//            leftLive.removeAll(phiLive);
            addToGraph(leftParent, leftLive);
        }

        // parse right parent branch
        if (lastBlock.isIfJoin) {
            rightLive.addAll(liveValues);
            addToGraph(lastBlock.parents.get(1), rightLive);
        }

    }

    private void addToPhiCluster(Instruction currentInstruction) {
        Set<Integer> cluster = new HashSet<>();

        addToCluster(currentInstruction, cluster);
        addToCluster(currentInstruction.getX(), cluster);
        addToCluster(currentInstruction.getY(), cluster);
    }

    private void addToCluster(Result currentInstruction, Set<Integer> cluster) {
        int valueNumber = getValueNumber(currentInstruction);
        int id;
        if (currentInstruction.kind == Kind.ADDR)
            id = ((Instruction) currentInstruction).phiVar.getId();
        else
            id = ((Variable) currentInstruction).getId();
        if (valueNumber != 0) {
            if (phiCluster.containsKey(id)) {
                cluster.addAll(phiCluster.get(id));
            }
            cluster.add(valueNumber);
            phiCluster.put(id, cluster);
        }
    }

    private boolean removeFromLive(Instruction currentInstruction, Set<Integer> liveValues) {
        if (liveValues.contains(currentInstruction.number)) {
            liveValues.remove(currentInstruction.number);
            return false;
        } else {
            currentInstruction.kill();
            return true;
        }
    }

    private void addToLive(Result x, Set<Integer> liveValues) {
        if (x == null) return;

        int n = getValueNumber(x);

        if (n == 0) return; // unassigned values are 0

        if (!interferenceGraph.containsKey(n)) {
            interferenceGraph.put(n, new HashSet<>());
        }

        for (int i : liveValues) {
            if (i != n) {
                if (interferenceGraph.containsKey(i)) {
                    interferenceGraph.get(i).add(n);
                }
                if (interferenceGraph.containsKey(n)) {
                    interferenceGraph.get(n).add(i);
                }
            }
        }

        liveValues.add(n);

    }

    private int getValueNumber(Result x) {
        if (x.kind == Kind.VAR) {
            return ((Variable) x).getValueLocation().number;
        } else if (x.kind == Kind.ADDR) {
            if (x instanceof BasicBlock) return 0; // TODO: handle compare statements where argument is a basic block
            return ((Instruction) x).getValueLocation().number;
        } else {
            return 0;
        }
    }

    public HashMap<Integer, Integer> allocate() {
        return buildInterferenceGraph(CFG.INSTANCE.getGraphs());
    }
}
