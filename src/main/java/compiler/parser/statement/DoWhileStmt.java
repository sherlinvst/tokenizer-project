package main.java.compiler.parser.statement;

import main.java.compiler.parser.ast.ASTNode;

public class DoWhileStmt extends ASTNode{
    public final ASTNode body;
    public final ASTNode condition;

    public DoWhileStmt(ASTNode body, ASTNode condition, int line, int col) {
        super(line, col);
        this.body = body;
        this.condition = condition;
    }
  
}
