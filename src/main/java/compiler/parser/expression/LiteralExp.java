package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class LiteralExp extends ASTNode {
    public final Token value;
    
    public LiteralExp(Token token){
        super(token.getLineNumber(), token.getColumnNumber());
        this.value = token;
    }
}
