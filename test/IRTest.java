package test;

import main.java.compiler.ir.ASTToIRConverter;
import main.java.compiler.ir.IRGenerator;
import main.java.compiler.parser.Parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;

import java.util.ArrayList;
import java.util.List;

public class IRTest {
    public static void main(String[] args) {
        // 1. Define the Tokens using the exact type strings expected by Parser.java
        // Note: Constructor order is (Lexeme, TokenType, Line, Column) per Token.java
        List<Token> tokens = new ArrayList<>();
        
        // {
        tokens.add(new Token("{", "Operator", 1, 1));
        
        // int a = 5;
        tokens.add(new Token("int", "Keyword", 2, 5));
        tokens.add(new Token("a", "Identifier", 2, 9));       // Parser expects "Identifier"
        tokens.add(new Token("=", "Operator", 2, 11));
        tokens.add(new Token("5", "Numeric Literal", 2, 13)); // Parser expects "Numeric Literal"
        tokens.add(new Token(";", "Operator", 2, 14));
        
        // int b = 10;
        tokens.add(new Token("int", "Keyword", 3, 5));
        tokens.add(new Token("b", "Identifier", 3, 9));
        tokens.add(new Token("=", "Operator", 3, 11));
        tokens.add(new Token("10", "Numeric Literal", 3, 13));
        tokens.add(new Token(";", "Operator", 3, 15));
        
        // int c = a + b * 2;
        tokens.add(new Token("int", "Keyword", 4, 5));
        tokens.add(new Token("c", "Identifier", 4, 9));
        tokens.add(new Token("=", "Operator", 4, 11));
        tokens.add(new Token("a", "Identifier", 4, 13));
        tokens.add(new Token("+", "Operator", 4, 15));
        tokens.add(new Token("b", "Identifier", 4, 17));
        tokens.add(new Token("*", "Operator", 4, 19));
        tokens.add(new Token("2", "Numeric Literal", 4, 21));
        tokens.add(new Token(";", "Operator", 4, 22));
        
        // a = c - 3;
        tokens.add(new Token("a", "Identifier", 5, 5));
        tokens.add(new Token("=", "Operator", 5, 7));
        tokens.add(new Token("c", "Identifier", 5, 9));
        tokens.add(new Token("-", "Operator", 5, 11));
        tokens.add(new Token("3", "Numeric Literal", 5, 13));
        tokens.add(new Token(";", "Operator", 5, 14));
        
        // }
        tokens.add(new Token("}", "Operator", 6, 1));
        
        // Add EOF Token to signify the end of the stream
        tokens.add(new Token("EOF", "EOF", 7, 1));

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

        // 2. Use your Parser to generate the AST
        Parser parser = new Parser(tokens);
        List<ASTNode> root = parser.parse();

        // 3. Check for errors
        if (!parser.getErrors().isEmpty()) {
            System.err.println("Parser encountered errors:");
            parser.getErrors().forEach(err -> System.err.println(err));
            return;
        }

        // 4. Run the IR Converter
        IRGenerator irGen = new IRGenerator();
        ASTToIRConverter converter = new ASTToIRConverter(irGen);

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
EXPECTED OUTPUT:

a = 5
b = 10
t1 = b * 2
t2 = a + t1
c = t2
t3 = c - 3
a = t3

*/