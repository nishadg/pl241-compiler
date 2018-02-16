package IR;

import Model.BasicBlock;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public enum CFG {
    INSTANCE;
    ArrayList<ArrayList<BasicBlock>> graphs = new ArrayList<>();
    private String fileName;
    public ArrayList<BasicBlock> currentGraph;

    public void addHead() {
        currentGraph = new ArrayList<>();
        graphs.add(currentGraph);
    }

    public void createDot() {
        for (ArrayList graph : graphs) {
            try {
                generateGraph(graph);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateGraph(ArrayList<BasicBlock> graph) throws IOException {
        deleteOld();
        RandomAccessFile file = new RandomAccessFile(getPath(fileName), "rw");
        file.writeBytes("digraph {");

        for (BasicBlock block : graph) {
            if (block.leftBlock != null)
                file.writeBytes(block.index + " -> " + block.leftBlock.index + "\n");
            if (block.rightBlock != null)
                file.writeBytes(block.index + " -> " + block.rightBlock.index + "\n");
        }
        file.writeBytes("\n}");
        file.close();
        Runtime.getRuntime().exec("dot " + getPath(fileName) + "  -Tpng -o ./VCG/" + fileName + ".png");
    }

    private void deleteOld() {
        File f = new File(getPath(fileName));
        f.delete();
    }

    private String getPath(String fileName) {
        return "./VCG/".concat(fileName).concat(".dot");
    }

    public void setName(String fileName) {
        this.fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));
    }


}
