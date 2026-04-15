package main.java.compiler.parser.ast;

public abstract class ASTNode {
    private int lineNumber;
    private int columnNumber;

    public ASTNode(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
