package RA;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Allocator {
    private final HashMap<Integer, Set<Integer>> interferenceGraph;
    private final HashMap<Integer, Set<Integer>> phiClusters;

    public Allocator(HashMap<Integer, Set<Integer>> interferenceGraph, HashMap<Integer, Set<Integer>> phiClusters) {
        this.interferenceGraph = interferenceGraph;
        this.phiClusters = phiClusters;
    }

    public HashMap<Integer, Integer> colorGraph() {
        HashMap<Integer, Integer> instructionColors = new HashMap<>(interferenceGraph.size());

        boolean colorUsed[] = new boolean[13];
        instructionColors.put(0, 0);

        //color all phi clusters with the same color
        for (Map.Entry<Integer, Set<Integer>> phiCluster : phiClusters.entrySet()) {
            // skip if already colored
            if (!instructionColors.containsKey(phiCluster.getKey())) {

                // mark color as used if neighbours are already colored
                for (int phiValue : phiCluster.getValue()) {
                    if (interferenceGraph.containsKey(phiValue))
                        for (int neighbour : interferenceGraph.get(phiValue)) {
                            if (instructionColors.containsKey(neighbour)) {
                                colorUsed[instructionColors.get(neighbour)] = true;
                            }
                        }
                }

                // search for unused color and mark all phi values with that color
                for (int i = 0; i < colorUsed.length; i++) {
                    if (!colorUsed[i]) {
                        for (int phiValue : phiCluster.getValue()) {
                            instructionColors.put(phiValue, i);
                        }
                        break;
                    }
                }
            }

            Arrays.fill(colorUsed, false);
        }

        // color all instructions based on neighbour colors
        for (Map.Entry<Integer, Set<Integer>> adjacentMap : interferenceGraph.entrySet()) {
            // skip if already colored
            if (!instructionColors.containsKey(adjacentMap.getKey())) {

                // mark colors as used if neighbours are colored
                for (int neighbour : adjacentMap.getValue()) {
                    if (instructionColors.containsKey(neighbour)) {
                        colorUsed[instructionColors.get(neighbour)] = true;
                    }
                }

                //search for unused color and mark instruction with that color.
                for (int i = 0; i < colorUsed.length; i++) {
                    if (!colorUsed[i]) {
                        instructionColors.put(adjacentMap.getKey(), i);
                        break;
                    }
                }
            }
            Arrays.fill(colorUsed, false);
        }
        return instructionColors;
    }
}
