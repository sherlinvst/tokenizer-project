package main.java.compiler.parser.type;
import java.util.ArrayList;

import main.java.compiler.parser.ast.ASTNode;

public class TypeNode extends ASTNode {
    public final String baseName;          // "List", "int", "Map"
    public final ArrayList<TypeNode> typeArgs; // empty if not generic
    public final int dimension;      // 0 = not array, 1 = [], 2 = [][]
    public TypeNode(String baseName, ArrayList<TypeNode> typeArgs, int arrayDimensions, int line, int col) {
        super(line, col);
        this.baseName = baseName;
        this.typeArgs = typeArgs;
        this.dimension = arrayDimensions;
    }
}
