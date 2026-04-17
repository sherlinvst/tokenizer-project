package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;

public class AssignExp extends ASTNode {
    public final ASTNode target; // was Token name — now any lvalue
    public final ASTNode value;
    public AssignExp(ASTNode target, ASTNode value) {
        super(target.lineNumber, target.columnNumber);
        this.target = target;
        this.value = value;
    }
}
