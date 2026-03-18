package main.java.tokenizer;

import main.java.model.Keywords;
import main.java.model.Operators;
import main.java.model.Separators;

public abstract class TokenRecognizer {
    private final String identifier = "Identifier";
    private final String invalidInput = "Invalid Input";

    private final Keywords keywords = new Keywords();
    private final Operators operators = new Operators();
    private final Separators separators = new Separators();

    private enum LiteralType {
        STRING, CHAR, NUMERIC, FLOAT, BOOLEAN, SPECIAL
    }

    protected String recognizeTokens(String lexeme){
        if(lexeme == null || lexeme.isEmpty()){
            return invalidInput;
        }

        String keywordToken = keywords.getToken(lexeme);
        if(keywordToken != null){
            return keywordToken;
        }

        String operatorToken = operators.getToken(lexeme);
        if(operatorToken != null){
            return operatorToken;
        }

        String separatorToken = separators.getToken(lexeme);
        if(separatorToken != null){
            return separatorToken;
        }

        String literalToken = getLiteralType(lexeme);
        if(literalToken != null){
            return literalToken;
        }

        if(isIdentifier(lexeme)){
            return identifier;
        }

        return invalidInput;
    }

    private String getLiteralType(String lexeme){
        if(lexeme.equals("null")){
            return LiteralType.SPECIAL.toString().toLowerCase() + "-literal";
        }

        if(lexeme.equals("true") || lexeme.equals("false")){
             return LiteralType.BOOLEAN.toString().toLowerCase() + "-literal";
        }

        if(lexeme.matches("^\".*\"$")){
             return LiteralType.STRING.toString().toLowerCase() + "-literal";
        }

        if(lexeme.matches("^'.'$")){
             return LiteralType.CHAR.toString().toLowerCase() + "-literal";
        }

        if(lexeme.matches("^[+-]?\\d+\\.\\d+$")){
             return LiteralType.FLOAT.toString().toLowerCase() + "-literal";
        }

        if(lexeme.matches("^[+-]?\\d+$")){
             return LiteralType.NUMERIC.toString().toLowerCase() + "-literal";
        }

        return null;
    }

    private boolean isIdentifier(String lexeme){
        return lexeme.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}

// Abstract class for token recognition logic

    // Methods and attributes in this class are tentatively planned and may be subject to change as development progresses.

    // Needed attribute:
    // 1. "Identifier" [private final String] - default token type for identifiers
    // 2. enum LiteralType {STRING, CHAR, NUMERIC, FLOAT, BOOLEAN, SPECIAL} - to determine literal type in getLiteralType method 
    // null is a SPECIAL literal type
    // https://www.geeksforgeeks.org/java/literals-in-java/ <- check here for more details on literals in Java
    // 3. "Invalid Input" [private final String] - default invalid token type if input doesn't match any token type oridentifier pattern
    // 4. Use keywords, operators, and separators model and their respective getToken method to retrive meaning of lexeme

    // Create a logic where it can recognize pattern
    // Example 1:
    // String name = "John Doe";
    //
    // String - data-type-keyword
    // name - identifier
    // = - assignment-operator
    // "John Doe" - string-literal
    //
    // Example 2:
    // while (true) {
    //    String var = "Hello world";
    //    break;
    // }
    //
    // while - keyword
    // ( - parenthesis
    // true - boolean-literal
    // ) - parenthesis
    // { - bracket
    // String - data-type-keyword
    // var - identifier
    // = - assignment-operator
    // "Hello world" - string-literal
    // ; - semicolon
    // break - keyword
    // } - bracket
    //
    // Example 3:
    // int `
    // 
    // int - data-type-keyword
    // ` - invalid input (doesn't match any token type or identifier pattern)
    //
    // Example 4:
    // public class HelloWorld {
    //     public static void main(String[] args) {
    //         System.out.println("Hello, World!");
    //     }
    // }
    //
    // public          - access-control-keyword
    // class           - object-oriented-keyword
    // HelloWorld      - identifier
    // {               - bracket
    // public          - access-control-keyword
    // static          - object-oriented-keyword
    // void            - data-type-keyword
    // main            - identifier
    // (               - parenthesis
    // String          - data-type-keyword
    // [               - bracket
    // ]               - bracket
    // args            - identifier
    // )               - parenthesis
    // {               - bracket
    // System          - identifier
    // .               - period
    // out             - identifier
    // .               - period
    // println         - identifier
    // (               - parenthesis
    // "Hello, World!" - string-literal
    // )               - parenthesis
    // ;               - semicolon
    // }               - bracket
    // }               - bracket

    // Needed method:
    // 1. recognizeTokens(lexeme) [protected String] - recognize input string and return token type {use getLLiteralType for literals and getToken for keywords, operators, separators} if model returns null, return "Identifier" as default token type for identifiers, if it doesn't match identifier pattern return "Invalid Input" 
    // 2. getLiteralType(lexeme) [private String] - to determine literal type (string, char, numeric) and return token type {use enum for literal types and convert to String for return value}
    // 3. isIdentifier(lexeme) [private boolean] - to check if lexeme matches identifier pattern (starts with letter or underscore, followed by letters, digits, or underscores)