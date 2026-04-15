package main.java.compiler.parser.expression;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class AssignExp extends ASTNode {
    public final ASTNode value;
    public final Token name;
    
    public AssignExp(Token name, ASTNode value){
        super(name.getLineNumber(), name.getColumnNumber());
        this.value = value;
        this.name = name;
    }
}
