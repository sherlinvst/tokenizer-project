package test;

import main.java.compiler.ir.*;
import java.util.List;

/**
 * A completely self-contained test file.
 * It uses a local dummy node to bypass ASTNode constructor issues.
 */
public class IRStandaloneTest {

    public static void main(String[] args) {
        IRGenerator irGen = new IRGenerator();
        
        // We use a local version of the converter that looks at our DummyNode
        StandaloneConverter converter = new StandaloneConverter(irGen);

        System.out.println("Building Mock AST for: result = (a + b) * (c - d)");

        // 1. Create Leaf Nodes
        DummyNode a = new DummyNode(null, null, null, "a");
        DummyNode b = new DummyNode(null, null, null, "b");
        DummyNode c = new DummyNode(null, null, null, "c");
        DummyNode d = new DummyNode(null, null, null, "d");

        // 2. Create Internal Nodes
        DummyNode add = new DummyNode("+", a, b, null);
        DummyNode sub = new DummyNode("-", c, d, null);
        DummyNode mul = new DummyNode("*", add, sub, null);

        // 3. Create Assignment Root
        DummyNode varRes = new DummyNode(null, null, null, "result");
        DummyNode root = new DummyNode("=", varRes, mul, null);

        // 4. Convert and Print
        System.out.println("Generating IR...");
        converter.convert(root);

        List<IRInstruction> instructions = irGen.getInstructions();
        System.out.println("\n--- Generated Three-Address Code ---");
        for (IRInstruction instr : instructions) {
            System.out.println(instr.toString());
        }
    }

    // A simple node structure that matches what your Converter expects via reflection
    static class DummyNode {
        public String operator;
        public DummyNode left;
        public DummyNode right;
        public String value;

        public DummyNode(String op, DummyNode l, DummyNode r, String val) {
            this.operator = op;
            this.left = l;
            this.right = r;
            this.value = val;
        }
    }

    // A specialized version of your converter that works with DummyNode
    static class StandaloneConverter {
        private IRGenerator irGen;

        public StandaloneConverter(IRGenerator irGen) { this.irGen = irGen; }

        public IRNode convert(DummyNode node) {
            if (node == null) return null;

            if ("=".equals(node.operator)) {
                IRNode rightIR = convert(node.right);
                if (node.left != null) {
                    irGen.generateAssignment(node.left.value, rightIR);
                }
                return null;
            }

            if (node.left == null && node.right == null) {
                return new IRNode(node.value);
            }

            IRNode leftIR = convert(node.left);
            IRNode rightIR = convert(node.right);
            return new IRNode(node.operator, leftIR, rightIR);
        }
    }
}