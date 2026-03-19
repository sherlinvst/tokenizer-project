package test;

import main.java.tokenizer.TokenRecognizer;

public class TokenRecognizerTest extends TokenRecognizer{
    public String classify(String lexeme){
        return recognizeTokens(lexeme);
    }
    public static void main(String[] args) {
        TokenRecognizerTest tester = new TokenRecognizerTest();
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
            "args",

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
