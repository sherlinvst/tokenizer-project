package main.java.model;

public class Token {
    private final String lexeme; // The actual string representation of the token inputted by the user
    private final String tokenType; // Must be in title case format
    private final int lineNumber; // The line number where the token is found in the source code, starting from 1

    public Token(String lexeme, String tokenType, int lineNumber) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.lineNumber = lineNumber;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
