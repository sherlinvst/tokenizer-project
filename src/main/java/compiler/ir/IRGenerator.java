package main.java.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {
    private List<IRInstruction> instructions = new ArrayList<>();
    private int tempCount = 0;
    private int labelCount = 0;

    public String nextTemp() { return "t" + (++tempCount); }
    public String nextLabel() { return "L" + (++labelCount); }

    public void emit(IRInstruction instr) {
        instructions.add(instr);
    }

    public void printIR() {
        for (IRInstruction instr : instructions) System.out.println(instr);
    }

    public void reset() {
        instructions.clear();
        tempCount = 0;
        labelCount = 0;
    }
}