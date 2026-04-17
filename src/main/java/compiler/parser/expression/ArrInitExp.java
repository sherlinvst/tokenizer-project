package main.java.compiler.parser.expression;
import java.util.ArrayList;

import main.java.compiler.parser.ast.ASTNode;

public class ArrInitExp extends ASTNode {
    public final ArrayList<ASTNode> elements;

    public ArrInitExp(ArrayList<ASTNode> elements, int line, int column) {
        super(line, column);
        this.elements = elements;
    }
}
