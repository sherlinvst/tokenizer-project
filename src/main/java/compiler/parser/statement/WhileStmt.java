package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;

public class WhileStmt extends ASTNode {
    public final ASTNode condition;
    public final ASTNode body;
    public WhileStmt(ASTNode condition, ASTNode body, int lineNumber, int columnNumber) {
        super(lineNumber, columnNumber);
        this.condition = condition;
        this.body = body;
    }

}
