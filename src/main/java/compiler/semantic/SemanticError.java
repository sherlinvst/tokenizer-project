package main.java.compiler.semantic;

public class SemanticError extends RuntimeException {

    private final int line;
    private final int col;

    public SemanticError(String message, int line, int col) {
        super(message);
        this.line = line;
        this.col  = col;
    }

    public int getLine() { return line; }
    public int getCol()  { return col; }

    public String toCompactString() {
        return String.format(
            "SEMANTIC ERROR: %s at line %d, column %d",
            getMessage(), line, col
        );
    }
}