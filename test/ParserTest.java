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

        lines.add("class ErrorTest { int x = ; }"); lines.add("class ErrorTest2 { public void test() { if (x > 0 { x++; } } }"); lines.add("class ErrorTest3 { for (int i = 0; i < 10 i++) { x++; } }"); lines.add("class ErrorTest4 { int y = (1 + 2; }");
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens();

        System.out.println("=== TOKENS ===");
        for (Token t : tokens) {
            System.out.println(t.getTokenType() + " " + t.getLexeme()+ " " + t.getLineNumber()+ " " + t.getColumnNumber()); 
        }

        System.out.println("\n=== AST ===");
        Parser parser = new Parser(tokens);
        ArrayList<ASTNode> ast = parser.parse();
        ParseDebug printer = new ParseDebug();
        for (ASTNode node : ast) {
            printer.print(node);
            System.out.println(); // spacing
        }

        for (ParseError node : parser.getErrors()) {
            System.out.println(node.getMessage()); // spacing
        }
        System.out.println("Parse successful. " + ast.size() + " top-level nodes.");
    }
    
}
