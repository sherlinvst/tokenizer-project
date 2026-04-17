package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class PostFixExp extends ASTNode {
    public final ASTNode operand;
    public final Token operator; 

    public PostFixExp(ASTNode operand, Token operator) {
        super(operator.getLineNumber(), operator.getColumnNumber());
        this.operand = operand;
        this.operator = operator;
    }
}
