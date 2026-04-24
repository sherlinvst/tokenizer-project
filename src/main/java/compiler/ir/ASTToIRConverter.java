package main.java.compiler.ir;

import main.java.compiler.parser.ast.ASTNode;
import java.lang.reflect.Field;

public class ASTToIRConverter {

    private IRGenerator irGen;

    public ASTToIRConverter(IRGenerator irGen) {
        this.irGen = irGen;
    }

    public IRNode convert(ASTNode node) {
        if (node == null) return null;

        // Try to safely extract fields via reflection (NO parser changes needed)
        String operator = getField(node, "operator");
        Object leftObj = getFieldObject(node, "left");
        Object rightObj = getFieldObject(node, "right");
        String value = getField(node, "value");

        ASTNode left = (leftObj instanceof ASTNode) ? (ASTNode) leftObj : null;
        ASTNode right = (rightObj instanceof ASTNode) ? (ASTNode) rightObj : null;

        // CASE 1: Assignment
        if ("=".equals(operator)) {
            IRNode rightIR = convert(right);

            if (left != null) {
                String varName = getField(left, "value");
                irGen.generateAssignment(varName, rightIR);
            }

            return null;
        }

        // CASE 2: Leaf node
        if (isLeaf(left, right)) {
            return new IRNode(value);
        }

        // CASE 3: Unary operation
        if (right == null && left != null) {
            IRNode leftIR = convert(left);
            return new IRNode(operator, leftIR);
        }

        // CASE 4: Binary operation
        IRNode leftIR = convert(left);
        IRNode rightIR = convert(right);

        return new IRNode(operator, leftIR, rightIR);
    }

    // ---- SAFE HELPERS (no parser editing needed) ----

    private String getField(ASTNode node, String fieldName) {
        try {
            Field field = node.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object val = field.get(node);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object getFieldObject(ASTNode node, String fieldName) {
        try {
            Field field = node.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(node);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isLeaf(ASTNode left, ASTNode right) {
        return left == null && right == null;
    }
}