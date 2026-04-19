package main.java.compiler.parser.statement;

import main.java.compiler.parser.ast.ASTNode;

public class ThrowStmt extends ASTNode{
    public final ASTNode value;

    public ThrowStmt(ASTNode value, int line, int col) {
        super(line, col);
        this.value = value;
    }
  
}