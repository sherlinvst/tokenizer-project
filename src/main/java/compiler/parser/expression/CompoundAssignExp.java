package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class CompoundAssignExp extends ASTNode{
    public final ASTNode target;
    public final Token operator;
    public final ASTNode value;

    public CompoundAssignExp(ASTNode target, Token operator, ASTNode value) {
        super(target.lineNumber, target.columnNumber);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }
}
