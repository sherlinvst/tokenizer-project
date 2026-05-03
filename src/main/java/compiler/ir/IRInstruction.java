package main.java.compiler.ir;

public class IRInstruction {

    public enum Type {
        // Original types
        ASSIGN, BINARY_OP, UNARY_OP, LABEL, JUMP, CONDITIONAL_JUMP,
        // New types
        CALL,           // method call
        FIELD_ACCESS,   // object.field
        NEW_OBJ,        // new ClassName(args)
        NEW_ARR,        // new Type[size]
        ARR_ACCESS,     // arr[index]
        CAST,           // (Type) operand
        RETURN,         // return value
        METHOD_START,   // start of method/constructor
        METHOD_END,     // end of method/constructor
        CLASS_START,    // start of class
        CLASS_END,      // end of class
        BREAK,          // break statement
        CONTINUE,       // continue statement
        THROW,          // throw statement
        ARR_INIT        // array initializer {1, 2, 3}
    }

    private Type type;
    private String op, arg1, arg2, result, label;

    // Binary Operation: result = arg1 op arg2
    // Also used for METHOD_START, CLASS_START, CALL, NEW_OBJ, NEW_ARR, FIELD_ACCESS, CAST, THROW, RETURN, ARR_ACCESS
    public IRInstruction(Type type, String op, String arg1, String arg2, String result) {
        this.type   = type;
        this.op     = op;
        this.arg1   = arg1;
        this.arg2   = arg2;
        this.result = result;
    }

    // Assignment / Unary: result = op arg1
    public IRInstruction(Type type, String op, String arg1, String result) {
        this.type   = type;
        this.op     = op;
        this.arg1   = arg1;
        this.result = result;
    }

    // Labels, Jumps, Break, Continue, Method/Class END
    public IRInstruction(Type type, String label) {
        this.type  = type;
        this.label = label;
    }

    // For CONDITIONAL_JUMP: arg1=condition, label=jumpTarget
    public IRInstruction(Type type, String arg1, String label) {
    this.type  = type;
    this.arg1  = arg1;
    this.label = label;
    }

    // --- Getters ---
    public Type   getType()   { return type; }
    public String getOp()     { return op; }
    public String getArg1()   { return arg1; }
    public String getArg2()   { return arg2; }
    public String getResult() { return result; }
    public String getLabel()  { return label; }

    @Override
    public String toString() {
        switch (type) {
            case BINARY_OP:        return result + " = " + arg1 + " " + op + " " + arg2;
            case ASSIGN:           return (result != null ? result : "?") + " = " + arg1;
            case UNARY_OP:         return result + " = " + op + arg1;
            case LABEL:            return label + ":";
            case JUMP:             return "GOTO " + label;
            case CONDITIONAL_JUMP: return "IF_FALSE " + arg1 + " GOTO " + label;
            case CALL:             return result + " = CALL " + op + "(" + (arg1 != null ? arg1 : "") + ")";
            case FIELD_ACCESS:     return result + " = " + arg1 + "." + op;
            case NEW_OBJ:          return result + " = NEW " + op + "(" + (arg1 != null ? arg1 : "") + ")";
            case NEW_ARR:          return result + " = NEW " + op + "[" + arg1 + "]";
            case ARR_ACCESS:       return result + " = " + arg1 + "[" + op + "]";
            case CAST:             return result + " = (" + op + ") " + arg1;
            case RETURN:           return "RETURN" + (arg1 != null ? " " + arg1 : "");
            case METHOD_START:     return "METHOD " + (op != null && !op.isEmpty() ? op + " " : "") + arg1 + "(" + (arg2 != null ? arg2 : "") + ") {";
            case METHOD_END:       return "} // end " + label;
            case CLASS_START:      return "CLASS " + (op != null && !op.isEmpty() ? op + " " : "") + arg1 + (arg2 != null ? " extends " + arg2 : "") + " {";
            case CLASS_END:        return "} // end class " + label;
            case BREAK:            return "BREAK";
            case CONTINUE:         return "CONTINUE";
            case THROW:            return "THROW " + arg1;
            case ARR_INIT:         return result + " = {" + arg1 + "}";
            default:               return "// unknown IR";
        }
    }
}