package main.java.compiler.parser.expression;
import java.util.ArrayList;

import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

public class MethodCallExp extends ASTNode {
    public final ASTNode object;
    public final Token method;
    public final ArrayList<ASTNode> arguments;
    
    public MethodCallExp(Token method, ASTNode object, ArrayList<ASTNode> arguments){
        super(method.getLineNumber(), method.getColumnNumber());
        this.method = method;
        this.object = object;
        this.arguments = arguments;
    }
}
