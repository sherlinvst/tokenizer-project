package main.java.compiler.semantic;

public class SemanticError {
    private final String message;
    private final int line;
    private final int col;

    public SemanticError(String message, int line, int col) {
        this.message = "SEMANTIC ERROR: " + message + " at line " + line + ", column " + col;
        this.line = line;
        this.col  = col;
    }

    public String getMessage() { return message; }
    public int getLine()       { return line; }
    public int getCol()        { return col; }
}
