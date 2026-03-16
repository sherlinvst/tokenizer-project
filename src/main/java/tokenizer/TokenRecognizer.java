package main.java.tokenizer;

public abstract class TokenRecognizer {
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

    // Needed method:
    // 1. recognizeTokens(lexeme) [protected String] - recognize input string and return token type {use getLLiteralType for literals and getToken for keywords, operators, separators} if model returns null, return "Identifier" as default token type for identifiers, if it doesn't match identifier pattern return "Invalid Input" 
    // 2. getLiteralType(lexeme) [private String] - to determine literal type (string, char, numeric) and return token type {use enum for literal types and convert to String for return value}
    // 3. isIdentifier(lexeme) [private boolean] - to check if lexeme matches identifier pattern (starts with letter or underscore, followed by letters, digits, or underscores)
}
