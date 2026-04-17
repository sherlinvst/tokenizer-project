package main.java.compiler.parser.error;
import main.java.compiler.parser.ast.ASTNode;

public class ErrorNode extends ASTNode {
    public ErrorNode (int line, int column){
        super(line, column);
    }
}
