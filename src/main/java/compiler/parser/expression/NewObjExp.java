package main.java.compiler.parser.expression;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;

public class NewObjExp extends ASTNode {
    public final TypeNode type;
    public final ArrayList<ASTNode> args;
    public NewObjExp(TypeNode type, ArrayList<ASTNode> args) {
        super(type.lineNumber, type.columnNumber);
        this.type = type;
        this.args = args;
    }
}
