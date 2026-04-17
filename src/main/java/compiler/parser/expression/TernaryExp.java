package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;

public class TernaryExp extends ASTNode {
    public final ASTNode condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch;
    
    public TernaryExp(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        super(condition.lineNumber, condition.columnNumber);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}
