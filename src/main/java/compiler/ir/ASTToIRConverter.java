package main.java.compiler.ir;

import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.declaration.*;
import java.util.List;

public class ASTToIRConverter {
    private IRGenerator irGen;

    public ASTToIRConverter(IRGenerator irGen) {
        this.irGen = irGen;
    }

    public void convert(List<ASTNode> nodes) {
        for (ASTNode node : nodes) convertNode(node);
    }

    public String convertNode(ASTNode node) {
        if (node == null) return null;

        // 1. Terminals (Leaves)
        if (node instanceof LiteralExp) {
            return ((LiteralExp) node).value.getLexeme();
        }
        if (node instanceof IdentifierExp) {
            return ((IdentifierExp) node).name.getLexeme();
        }

        // 2. Binary Expressions (a + b)
        if (node instanceof BinaryExp) {
            BinaryExp bin = (BinaryExp) node;
            String left = convertNode(bin.left);
            String right = convertNode(bin.right);
            String target = irGen.nextTemp();
            irGen.emit(new IRInstruction(IRInstruction.Type.BINARY_OP, bin.operator.getLexeme(), left, right, target));
            return target;
        }

        // 3. Assignments (x = value)
        if (node instanceof AssignExp) {
            AssignExp assign = (AssignExp) node;
            String val = convertNode(assign.value);
            if (assign.target instanceof IdentifierExp) {
                String varName = ((IdentifierExp) assign.target).name.getLexeme();
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, varName));
            }
            return null;
        }

        // 4. Statements
        if (node instanceof ExprStmt) {
            convertNode(((ExprStmt) node).exp);
        } else if (node instanceof VarDecl) {
            VarDecl decl = (VarDecl) node;
            if (decl.init != null) {
                String val = convertNode(decl.init);
                irGen.emit(new IRInstruction(IRInstruction.Type.ASSIGN, null, val, decl.name.getLexeme()));
            }
        } else if (node instanceof BlockStmt) {
            for (ASTNode s : ((BlockStmt) node).statements) convertNode(s);
        }

        return null;
    }
}