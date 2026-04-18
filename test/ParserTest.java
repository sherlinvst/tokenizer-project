package test;

import main.java.compiler.parser.ParseError;
import java.util.ArrayList;
import main.java.compiler.parser.Parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;

public class ParserTest {
    public static void main(String[] args) {
        ArrayList<String> lines = new ArrayList<>();

        // ===== VALID CASES =====
        lines.add("class A { int x; }");

        lines.add("class B { int x = 5; int y = 10; }");

        lines.add("class C { int add(int a, int b) { return a + b; } }");

        lines.add("class D { void test() { int x = 0; x = x + 1; } }");

        lines.add("class E { void loop() { for (int i = 0; i < 3; i++) { x += i; } } }");

        lines.add("class F { int x; void inc() { x++; ++x; } }");

        lines.add("class G { int[] arr = new int[5]; }");

        lines.add("class H { int x; void test() { if (x > 0) { x = 1; } else { x = 2; } } }");

        // ===== ERROR CASES =====
        lines.add("class Err1 { int x = ; }"); // missing expression

        lines.add("class Err2 { void t() { if (x > 0 { x++; } } }"); // missing ')'

        lines.add("class Err3 { for (int i = 0; i < 10 i++) { x++; } }"); // missing ';'

        lines.add("class Err4 { int y = (1 + 2; }"); // missing ')'

        lines.add("pubic class Err5 { int x; }"); // invalid modifier
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens();

        System.out.println("=== TOKENS ===");
        for (Token t : tokens) {
            System.out.println(t.toString());
        }

        System.out.println("\n=== AST ===");
        Parser parser = new Parser(tokens);
        
        ArrayList<ASTNode> ast = parser.parse();
        ParseDebug printer = new ParseDebug();
        for (ASTNode node : ast) {
            printer.print(node);
            System.out.println(); // spacing
        }
        System.out.println("Parse successful. " + ast.size() + " top-level nodes.");
        
        for (ParseError error : parser.getErrors()) {
            System.out.println(error.getMessage());
        }
        
    }
    
}
