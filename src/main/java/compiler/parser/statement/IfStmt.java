package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;

public class IfStmt extends ASTNode {
    public final ASTNode condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch;
    
    public IfStmt(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch, int lineNumber, int columnNumber){
        super(lineNumber, columnNumber);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}
