package main.java.compiler.parser.statement;
import main.java.compiler.parser.ast.ASTNode;
import java.util.ArrayList;

public class BlockStmt extends ASTNode {
    public final ArrayList<ASTNode> statements;
    
    public BlockStmt(ArrayList<ASTNode> statements, int lineNumber, int columnNumber){
        super(lineNumber, columnNumber);
        this.statements = statements;
    }
}
