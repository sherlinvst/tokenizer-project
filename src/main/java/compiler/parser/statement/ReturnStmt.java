package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;

public class ReturnStmt extends ASTNode {
    public final ASTNode value;
    
    public ReturnStmt(ASTNode value, int lineNumber, int colNumber) {
        super(lineNumber, colNumber);
        this.value = value;
    }
}
