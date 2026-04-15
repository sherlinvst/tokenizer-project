package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;

public class ExprStmt extends ASTNode {
    public final ASTNode exp;
    
    public ExprStmt(ASTNode exp, int lineNumber, int colNumber) {
        super(lineNumber, colNumber);
        this.exp = exp;
    }
}
