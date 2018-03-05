package test;

import Model.BasicBlock;
import Model.Instruction;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DotGenerator {

    private String fileName;
    private RandomAccessFile file;
    private String dominatorGraphStyle = " [style = dotted fillcolor = red arrowhead=inv]";

    public DotGenerator(String name) {
        this.fileName = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
    }

    private void generateGraph(List<BasicBlock> graph, String name) throws IOException {
        for (BasicBlock block : graph) {
            String blockLabel = "";
            if (block.isWhileJoin) blockLabel = "While ";
            if (block.isIfJoin) blockLabel = "Fi ";
            file.writeBytes(block.blockIndex + " [ label = \"" + blockLabel + "Block " + block.blockIndex + "\\l" + getInstructionsString(block) + "\" shape = \"box\"]");
            if (block.leftBlock != null)
                file.writeBytes(block.blockIndex + " -> " + block.leftBlock.blockIndex + "\n");
            if (block.rightBlock != null)
                file.writeBytes(block.blockIndex + " -> " + block.rightBlock.blockIndex + "\n");

            // Dominator edges
            if (!block.parents.isEmpty()) {
                if (block.isIfJoin) {
                    file.writeBytes(block.ifParentBlock.blockIndex + " -> " + block.blockIndex + dominatorGraphStyle);
                } else {
                    file.writeBytes(block.parents.get(0).blockIndex + " -> " + block.blockIndex + dominatorGraphStyle);
                }
            }
        }
    }

    private String getInstructionsString(BasicBlock block) {
        return implode("\\l", block.getInstructionList());
    }

    private String getPath(String fileName) {
        return "./VCG/".concat(fileName).concat(".dot");
    }


    private void deleteOld() {
        File f = new File(getPath(fileName));
        f.delete();
    }

    public void generateCFG(List<List<BasicBlock>> graphs) throws IOException {
        deleteOld();
        file = new RandomAccessFile(getPath(fileName), "rw");
        file.writeBytes("digraph " + fileName + "{");
        for (List<BasicBlock> graph : graphs) {
            generateGraph(graph, graph.get(0).name);
        }
        file.writeBytes("\n}");
        file.close();
        Runtime.getRuntime().exec("dot " + getPath(fileName) + "  -Tpng -o ./VCG/" + fileName + ".png");
    }

    private String implode(String glue, List<Instruction> instructions) {
        StringBuilder str = new StringBuilder();
        for (Instruction i : instructions) {
            str.append(i.outputString());
            str.append(glue);
        }
        return str.toString();
    }
}
