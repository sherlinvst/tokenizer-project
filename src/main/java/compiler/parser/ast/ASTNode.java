package main.java.compiler.parser.ast;

public abstract class ASTNode {
    public final int lineNumber;
    public final int columnNumber;

    public ASTNode(int lineNumber, int columnNumber) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }
}
