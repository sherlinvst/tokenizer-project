package main.java.compiler.parser.declaration;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;
import main.java.model.Token;

public class ClassDecl extends ASTNode {
    public final Token name;
    public final TypeNode superClass;        
    public final ArrayList<TypeNode> interfaces;
    public final ArrayList<ASTNode> members;
    public final ArrayList<Token> modifiers;

    public ClassDecl(ArrayList<Token> modifiers, Token name, TypeNode superClass, ArrayList<TypeNode> interfaces, ArrayList<ASTNode> members) {
        super(name.getLineNumber(), name.getColumnNumber());
        this.modifiers = modifiers;
        this.name = name;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.members = members;
    }
}
