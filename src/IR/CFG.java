package IR;

import Model.BasicBlock;
import test.DotGenerator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public enum CFG {
    INSTANCE;
    ArrayList<ArrayList<BasicBlock>> graphs = new ArrayList<>();
    private ArrayList<BasicBlock> currentGraph;
    private String name;

    public void addGraph() {
        currentGraph = new ArrayList<>();
        graphs.add(currentGraph);
    }

    public BasicBlock resetCurrentGraphToMain() {
        currentGraph = graphs.get(0);
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
