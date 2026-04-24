package main.java.compiler.ir;

public class IRNode {

    public String value;      // for variables or constants
    public String operator;   // +, -, *, /, !, etc.
    public IRNode left;
    public IRNode right;

    // Leaf node (variable or number)
    public IRNode(String value) {
        this.value = value;
    }

    // Binary operation
    public IRNode(String operator, IRNode left, IRNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    // Unary operation
    public IRNode(String operator, IRNode operand) {
        this.operator = operator;
        this.left = operand;
        this.right = null;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}