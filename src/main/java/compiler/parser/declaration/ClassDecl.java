package main.java.compiler.parser.declaration;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class ClassDecl extends ASTNode {
    public final Token name;
    public final ArrayList<ASTNode> members;

    public ClassDecl(Token name, ArrayList<ASTNode> members) {
        super(name.getLineNumber(), name.getColumnNumber());
        this.name = name; 
        this.members = members;
    }
}
