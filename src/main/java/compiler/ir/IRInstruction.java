package main.java.compiler.ir;

public class IRInstruction {
    public enum Type {
        ASSIGN, BINARY_OP, UNARY_OP, LABEL, JUMP, CONDITIONAL_JUMP
    }

    private Type type;
    private String op, arg1, arg2, result, label;

    // Binary Operation: res = arg1 op arg2
    public IRInstruction(Type type, String op, String arg1, String arg2, String result) {
        this.type = type;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    // Assignment / Unary: res = op arg1
    public IRInstruction(Type type, String op, String arg1, String result) {
        this.type = type;
        this.op = op;
        this.arg1 = arg1;
        this.result = result;
    }

    // Labels and Jumps
    public IRInstruction(Type type, String label) {
        this.type = type;
        this.label = label;
    }

    @Override
    public String toString() {
        switch (type) {
            case BINARY_OP: return result + " = " + arg1 + " " + op + " " + arg2;
            case ASSIGN:    return result + " = " + arg1;
            case UNARY_OP:   return result + " = " + op + arg1;
            case LABEL:     return label + ":";
            case JUMP:      return "GOTO " + label;
            case CONDITIONAL_JUMP: return "IF_FALSE " + arg1 + " GOTO " + label;
            default: return "";
        }
    }
}