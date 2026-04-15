package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class IdentifierExp extends ASTNode {
    public final Token name;
    
    public IdentifierExp(Token token){
        super(token.getLineNumber(), token.getColumnNumber());
        this.name = token;
    }
}
