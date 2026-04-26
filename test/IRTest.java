package test;

import main.java.compiler.ir.ASTToIRConverter;
import main.java.compiler.ir.IRGenerator;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.parser.expression.*;
import main.java.compiler.parser.statement.*;
import main.java.compiler.parser.declaration.*;
import main.java.compiler.parser.type.TypeNode;
import main.java.model.Token;

import java.util.ArrayList;
import java.util.List;

public class IRTest {
    public static void main(String[] args) {
        // 1. Setup IR Generator and Converter
        IRGenerator irGen = new IRGenerator();
        ASTToIRConverter converter = new ASTToIRConverter(irGen);

        System.out.println("=========================================");
        System.out.println("===       SIMULATED SOURCE CODE       ===");
        System.out.println("=========================================");
        System.out.println("{");
        System.out.println("    int a = 5;");
        System.out.println("    int b = 10;");
        System.out.println("    int c = a + b * 2;");
        System.out.println("    a = c - 3;");
        System.out.println("}");
        System.out.println("=========================================\n");

        // 2. Base Type Configuration
        TypeNode intType = new TypeNode("int", new ArrayList<TypeNode>(), 0, 1, 1);
        ArrayList<Token> noModifiers = new ArrayList<>();

        // 3. Mocking Tokens & Building AST Nodes

        // --- Line 1: int a = 5; ---
        Token t_a = new Token("IDENTIFIER", "a", 2, 9);
        Token t_5 = new Token("NUMBER", "5", 2, 13);
        VarDecl declA = new VarDecl(noModifiers, intType, t_a, new LiteralExp(t_5));

        // --- Line 2: int b = 10; ---
        Token t_b = new Token("IDENTIFIER", "b", 3, 9);
        Token t_10 = new Token("NUMBER", "10", 3, 13);
        VarDecl declB = new VarDecl(noModifiers, intType, t_b, new LiteralExp(t_10));

        // --- Line 3: int c = a + b * 2; ---
        Token t_c = new Token("IDENTIFIER", "c", 4, 9);
        Token t_plus = new Token("OPERATOR", "+", 4, 15);
        Token t_mult = new Token("OPERATOR", "*", 4, 19);
        Token t_2 = new Token("NUMBER", "2", 4, 21);
        
        // Remember PEMDAS! The AST groups multiplication first: (b * 2)
        BinaryExp multExp = new BinaryExp(t_mult, new IdentifierExp(t_b), new LiteralExp(t_2));
        // Then the addition wraps it: a + (b * 2)
        BinaryExp addExp = new BinaryExp(t_plus, new IdentifierExp(t_a), multExp);
        VarDecl declC = new VarDecl(noModifiers, intType, t_c, addExp);

        // --- Line 4: a = c - 3; ---
        Token t_minus = new Token("OPERATOR", "-", 5, 11);
        Token t_3 = new Token("NUMBER", "3", 5, 13);
        
        BinaryExp subExp = new BinaryExp(t_minus, new IdentifierExp(t_c), new LiteralExp(t_3));
        AssignExp assignA = new AssignExp(new IdentifierExp(t_a), subExp);
        ExprStmt stmtA = new ExprStmt(assignA, 5, 5);

        // 4. Combine into a Block Statement
        ArrayList<ASTNode> blockStatements = new ArrayList<>();
        blockStatements.add(declA);
        blockStatements.add(declB);
        blockStatements.add(declC);
        blockStatements.add(stmtA);
        
        BlockStmt mainBlock = new BlockStmt(blockStatements, 1, 1);

        // Wrap the block in a root list (what the converter expects)
        List<ASTNode> root = new ArrayList<>();
        root.add(mainBlock);

        // 5. Run the Converter
        System.out.println("=========================================");
        System.out.println("=== GENERATED IR (THREE-ADDRESS CODE) ===");
        System.out.println("=========================================");
        try {
            converter.convert(root);
            irGen.printIR();
        } catch (Exception e) {
            System.err.println("Error during IR conversion:");
            e.printStackTrace();
        }
        System.out.println("=========================================");
    }
}

/*
OUTPUT:
a = 5
b = 10
t1 = b * 2
t2 = a + t1
c = t2
t3 = c - 3
a = t3 */