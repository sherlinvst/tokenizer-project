package test;

import main.java.model.Keywords;
import main.java.model.Operators;
import main.java.model.Separators;
import main.java.tokenizer.TokenRecognizer;

public class ModelTestFile extends TokenRecognizer {
    public String classify(String lexeme){
        return recognizeTokens(lexeme);
    }

    public static void main(String[] args) {
        ModelTestFile tester = new ModelTestFile();
        // Keywords keywords = new Keywords();
        // System.out.println(keywords.getToken("if"));
        // Operators operator = new Operators();
        // System.out.println(operator.getToken("="));
        // Separators separator = new Separators();
        // System.out.println(separator.getToken(";"));
        
        String[] test = {
            "Example 1",
            "public",
            "class",
            "HelloWorld",
            "{",
            "public",
            "static",
            "void",
            "main",
            "(",
            "String",
            "[",
            "]",
            ")",
            "{",
            "System",
            ".",
            "out",
            ".",
            "println",
            "(",
            "\"Hello, World!\"",
            ")",
            ";",
            "}",
            "}", 
            //"args",

            "Example 2",
            "while",
            "(",
            "true",
            ")",
            "{",
            "String",
            "var",
            "=",
            "\"Hello world\"",
            ";",
            "break",
            "}",

            "Example 3",
            "String",
            "name",
            "=",
            "\"John Doe\"",
            ";"
        };

        for (String lexeme : test) {
            System.out.println(lexeme + " -> " + tester.classify(lexeme));
        }
    }
}
