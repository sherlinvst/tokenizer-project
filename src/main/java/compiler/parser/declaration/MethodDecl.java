package main.java.compiler.parser.declaration;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.statement.BlockStmt;
import main.java.model.Token;

public class MethodDecl extends ASTNode {
    public final Token type;
    public final Token name;
    public final ArrayList<VarDecl> params;
    public final BlockStmt body;

    public MethodDecl(Token type, Token name, ArrayList<VarDecl> params, BlockStmt body) {
        super(type.getLineNumber(), type.getColumnNumber());
        this.type = type;
        this.name = name;
        this.params = params;
        this.body = body;
    }
}
