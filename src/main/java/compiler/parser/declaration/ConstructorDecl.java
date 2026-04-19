package main.java.compiler.parser.declaration;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.statement.BlockStmt;
import main.java.compiler.parser.type.TypeNode;
import main.java.model.Token;

public class ConstructorDecl extends ASTNode{
    public final ArrayList<Token> modifiers;
    public final Token name;
    public final ArrayList<VarDecl> params;
    public final ArrayList<TypeNode> throwClause;
    public final BlockStmt body;

    public ConstructorDecl(ArrayList<Token> modifiers, Token name, ArrayList<VarDecl> params, ArrayList<TypeNode> throwClause, BlockStmt body) {
      super(name.getLineNumber(), name.getColumnNumber());
        this.modifiers = modifiers;
        this.name = name;
        this.params = params;
        this.throwClause = throwClause;
        this.body = body;
    }
  
}
