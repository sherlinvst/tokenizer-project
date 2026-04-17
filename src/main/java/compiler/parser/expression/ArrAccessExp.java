package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;

public class ArrAccessExp extends ASTNode {
    public final ASTNode array;
    public final ASTNode index;
    
    public ArrAccessExp(ASTNode array, ASTNode index) {
        super(array.lineNumber, array.columnNumber);
        this.array = array;
        this.index = index;
    }
}