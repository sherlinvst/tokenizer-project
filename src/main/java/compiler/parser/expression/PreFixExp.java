package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class PreFixExp extends ASTNode {
    public final Token operator; // ++ or --
    public final ASTNode operand;

    public PreFixExp(Token operator, ASTNode operand) {
        super(operator.getLineNumber(), operator.getColumnNumber());
        this.operator = operator;
        this.operand = operand;
    }
}
