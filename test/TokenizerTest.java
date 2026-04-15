package test;

import main.java.tokenizer.Tokenizer;
import main.java.model.Token;
import java.util.ArrayList;

public class TokenizerTest {
    public static void main(String[] args) {
        
        // 1. Setup: Create an instance of your Tokenizer tool
        Tokenizer myTokenizer = new Tokenizer();
        ArrayList<String> codeLines = new ArrayList<>();

        // 2. The Input: Imagine this is a line from a Java file we want to analyze
        String codeLine1 = "int myNumber = 10.0";
        String codeLine2 = "int myNumber = 200";

        codeLines.add(codeLine1);
        codeLines.add(codeLine2);

        System.out.println("Testing lines:\n");
        for (String line : codeLines) {
            System.out.println(line);
        }
        System.out.println("------------------------------");

        // 3. Action: Call the 'tokenize' method you wrote
        myTokenizer.tokenizeLines(codeLines, 1);

        // 4. Retrieval: Get the list of tokens back from the tokenizer
        ArrayList<Token> results = myTokenizer.getTokens();

        // 5. Output: Loop through the results and print them to the terminal
        if (results == null || results.isEmpty()) {
            System.out.println("No tokens found. Check your logic!");
        } else {
            for (Token t : results) {
                System.out.println("Token: [" + t.getLexeme() + "] | Type: " + t.getTokenType());
            }
        }
    }
}