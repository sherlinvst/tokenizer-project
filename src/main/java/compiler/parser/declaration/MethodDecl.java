package main.java.compiler.parser.declaration;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.statement.BlockStmt;
import main.java.compiler.parser.type.TypeNode;
import main.java.model.Token;

public class MethodDecl extends ASTNode {
    public final TypeNode type;
    public final Token name;
    public final ArrayList<TypeNode> throwsClause;
    public final ArrayList<VarDecl> params;
    public final ArrayList<Token> modifiers;
    public final BlockStmt body;

    public MethodDecl(ArrayList<Token> modifiers, TypeNode type, Token name, ArrayList<TypeNode> throwsClause, ArrayList<VarDecl> params, BlockStmt body) {
        super(type.lineNumber, type.columnNumber);
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.throwsClause = throwsClause;
        this.params = params;
        this.body = body;
    }
}
