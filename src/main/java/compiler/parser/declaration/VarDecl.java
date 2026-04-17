package main.java.compiler.parser.declaration;
import java.util.ArrayList;

import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;
import main.java.model.Token;

public class VarDecl extends ASTNode {
    public final ArrayList<Token> modifiers;
    public final TypeNode type;
    public final Token name;
    public final ASTNode init;

    public VarDecl(ArrayList<Token> modifiers, TypeNode type, Token name, ASTNode init) {
        super(type.lineNumber, type.columnNumber);
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.init = init;
    }
}
