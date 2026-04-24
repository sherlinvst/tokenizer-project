package main.java.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {

    private List<IRInstruction> instructions = new ArrayList<>();
    private int tempCount = 0;
    private int labelCount = 0;

    // PUBLIC temp generator
    public String nextTemp() {
        return "t" + (++tempCount);
    }

    // Label generator (for control flow)
    public String nextLabel() {
        return "L" + (++labelCount);
    }

    // Reset state (important for multiple runs)
    public void reset() {
        instructions.clear();
        tempCount = 0;
        labelCount = 0;
    }

    public List<IRInstruction> getInstructions() {
        return instructions;
    }

    // MAIN GENERATE FUNCTION (now uses IRNode)
    public String generate(IRNode node) {
        if (node == null) return null;

        // Leaf node (variable or constant)
        if (node.isLeaf()) {
            return node.value;
        }

        // Unary operation (e.g., -a, !b)
        if (node.right == null) {
            String operand = generate(node.left);
            String temp = nextTemp();

            instructions.add(new IRInstruction(
                    IRInstruction.Type.UNARY_OP,
                    node.operator,
                    operand,
                    null,
                    temp
            ));

            return temp;
        }

        // Binary operation (e.g., a + b, c * d)
        String left = generate(node.left);
        String right = generate(node.right);

        String temp = nextTemp();

        instructions.add(new IRInstruction(
                IRInstruction.Type.BINARY_OP,
                node.operator,
                left,
                right,
                temp
        ));

        return temp;
    }

    // Assignment (a = expression)
    public void generateAssignment(String variable, IRNode expr) {
        String value = generate(expr);

        instructions.add(new IRInstruction(
                IRInstruction.Type.ASSIGN,
                value,
                variable
        ));
    }

    // IF condition jump (basic control flow)
    public void generateIf(String conditionTemp, String label) {
        instructions.add(new IRInstruction(
                IRInstruction.Type.CONDITIONAL_JUMP,
                conditionTemp,
                label,
                true
        ));
    }

    // Unconditional jump
    public void generateGoto(String label) {
        instructions.add(new IRInstruction(
                IRInstruction.Type.JUMP,
                label
        ));
    }

    // Label marker
    public void generateLabel(String label) {
        instructions.add(new IRInstruction(
                IRInstruction.Type.LABEL,
                label
        ));
    }

    public void addInstruction(IRInstruction instr) {
    instructions.add(instr);
    }
}

