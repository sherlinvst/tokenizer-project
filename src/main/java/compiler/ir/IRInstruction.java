package main.java.compiler.ir;

public class IRInstruction {

    public enum Type {
        ASSIGN,
        BINARY_OP,
        UNARY_OP,
        LABEL,
        JUMP,
        CONDITIONAL_JUMP
    }

    private Type type;
    private String op;
    private String arg1;
    private String arg2;
    private String result;
    private String label;

    // Constructor for binary operations
    public IRInstruction(Type type, String op, String arg1, String arg2, String result) {
        this.type = type;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    // Constructor for assignment or unary
    public IRInstruction(Type type, String arg1, String result) {
        this.type = type;
        this.arg1 = arg1;
        this.result = result;
    }

    // Constructor for label
    public IRInstruction(Type type, String label) {
        this.type = type;
        this.label = label;
    }

    // Constructor for jumps
    public IRInstruction(Type type, String condition, String label, boolean isConditional) {
        this.type = type;
        this.arg1 = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        switch (type) {
            case BINARY_OP:
                return result + " = " + arg1 + " " + op + " " + arg2;

            case UNARY_OP:
                return result + " = " + op + arg1;

            case ASSIGN:
                return result + " = " + arg1;

            case LABEL:
                return label + ":";

            case JUMP:
                return "GOTO " + label;

            case CONDITIONAL_JUMP:
                return "IF " + arg1 + " GOTO " + label;

            default:
                return "UNKNOWN";
        }
    }
}