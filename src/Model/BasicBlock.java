package Model;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    List<Instruction> instructionList = new ArrayList<>();
    BasicBlock leftBlock;
    BasicBlock rightBlock;

}
