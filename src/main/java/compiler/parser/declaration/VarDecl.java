package main.java.compiler.parser.declaration;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class VarDecl extends ASTNode {
    public final Token type;
    public final Token name;
    public final ASTNode init;

    public VarDecl(Token type, Token name, ASTNode init) {
        super(type.getLineNumber(), type.getColumnNumber());
        this.type = type;
        this.name = name;
        this.init = init;
    }
}
