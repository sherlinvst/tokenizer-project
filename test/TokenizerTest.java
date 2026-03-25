package test;

import main.java.tokenizer.Tokenizer;
import main.java.model.Token;
import java.util.ArrayList;

public class TokenizerTest {
    public static void main(String[] args) {
        
        // 1. Setup: Create an instance of your Tokenizer tool
        Tokenizer myTokenizer = new Tokenizer();

        // 2. The Input: Imagine this is a line from a Java file we want to analyze
        String codeLine = "int myNumber = 100";

        System.out.println("Testing line: " + codeLine);
        System.out.println("------------------------------");

        // 3. Action: Call the 'tokenize' method you wrote
        myTokenizer.tokenize(codeLine);

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