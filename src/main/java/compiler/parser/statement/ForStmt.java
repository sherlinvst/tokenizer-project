package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;

public class ForStmt extends ASTNode {
    public final ASTNode init;
    public final ASTNode condition;
    public final ASTNode update;
    public final ASTNode body; 
    
    public ForStmt(ASTNode init, ASTNode condition, ASTNode update, ASTNode body, int lineNumber, int colNumber) {
        super(lineNumber, colNumber);
        this.init = init; 
        this.condition = condition;
        this.update = update; 
        this.body = body;
    }
}
