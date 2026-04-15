package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class UnaryExp extends ASTNode {
    public final ASTNode operand;
    public final Token operator;
    
    public UnaryExp(Token operator, ASTNode operand){
        super(operator.getLineNumber(), operator.getColumnNumber());
        this.operator = operator;
        this.operand = operand;
    }
}
