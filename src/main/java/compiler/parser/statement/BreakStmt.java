package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class BreakStmt extends ASTNode{
    public final Token label;

    public BreakStmt(Token label, int line, int col) {
        super(line, col);
        this.label = label;
    }
  
}
