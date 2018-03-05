package IR;

import Model.BasicBlock;
import test.DotGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum CFG {
    INSTANCE;

    public List<List<BasicBlock>> getGraphs() {
        return graphs;
    }

    List<List<BasicBlock>> graphs = new ArrayList<>();
    private ArrayList<BasicBlock> currentGraph;
    private String name;

    public void addGraph() {
        currentGraph = new ArrayList<>();
        graphs.add(currentGraph);
    }

    public BasicBlock resetCurrentGraphToMain() {
        currentGraph = (ArrayList<BasicBlock>) graphs.get(0);
        return currentGraph.get(0);
    }

    public void createDotFile() {
        try {
            DotGenerator dotGenerator = new DotGenerator(name);
            dotGenerator.generateCFG(graphs);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addToCurrentGraph(BasicBlock b) {
        currentGraph.add(b);
    }

    public void setName(String name) {
        this.name = name;
    }
}
