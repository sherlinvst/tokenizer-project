package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.type.TypeNode;

public class CastExp extends ASTNode {
    public final TypeNode targetType;
    public final ASTNode operand;
    public CastExp(TypeNode targetType, ASTNode operand) {
        super(targetType.lineNumber, targetType.columnNumber);
        this.targetType = targetType;
        this.operand = operand;
    }
}
