package main.java.compiler.parser.expression;
import java.util.ArrayList;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;

public class NewArrExp extends ASTNode {
    public final TypeNode elementType;
    public final ASTNode size;           // null if initializer list used
    public final ArrayList<ASTNode> initializer; // null if size used
    public NewArrExp(TypeNode elementType, ASTNode size, ArrayList<ASTNode> initializer) {
        super(elementType.lineNumber, elementType.columnNumber);
        this.elementType = elementType;
        this.size = size;
        this.initializer = initializer;
    }
}
