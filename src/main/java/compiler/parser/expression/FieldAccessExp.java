package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;


public class FieldAccessExp extends ASTNode {
    public ASTNode target;   // object (e.g., a in a.b)
    public Token field;      // field name (e.g., b)

    public FieldAccessExp(ASTNode target, Token field) {
        super(field.getLineNumber(), field.getColumnNumber());
        this.target = target;
        this.field = field;
    }
}
