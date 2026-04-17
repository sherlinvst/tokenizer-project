package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class SuperExp extends ASTNode {
    public final Token value;

    public SuperExp(Token value) {
        super(value.getLineNumber(), value.getColumnNumber());
        this.value = value;
    }
}
