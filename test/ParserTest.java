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

        // Test 7: intentional error — missing semicolon
        lines.add("x = 5");  // should throw ParseException
        Tokenizer tokenizer = new Tokenizer();
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens();

        System.out.println("=== TOKENS ===");
        for (Token t : tokens) {
            System.out.println(t.getTokenType() + " " + t.getLexeme()+ " " + t.getLineNumber()+ " " + t.getColumnNumber()); 
        }

        System.out.println("\n=== AST ===");
        Parser parser = new Parser(tokens);
        try {
            ArrayList<ASTNode> ast = parser.parse();
            ParseDebug printer = new ParseDebug();
            for (ASTNode node : ast) {
                printer.print(node);
                System.out.println(); // spacing
            }
            System.out.println("Parse successful. " + ast.size() + " top-level nodes.");
        } catch (ParseError e) {
            System.out.println("PARSE ERROR: " + e.getMessage());
        }
    }
    
}
