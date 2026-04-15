package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class BinaryExp extends ASTNode {
    public final ASTNode left, right;
    public final Token operator;
    
    public BinaryExp(Token binary, ASTNode left, ASTNode right){
        super(binary.getLineNumber(), binary.getColumnNumber());
        this.operator = binary;
        this.left = left;
        this.right = right;
    }
}
