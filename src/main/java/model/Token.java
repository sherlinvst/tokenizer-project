package main.java.model;

public class Token {
    private final String lexeme; // The actual string representation of the token inputted by the user
    private final String tokenType; // Must be in title case format
    private final int lineNumber;
    private final int columnNumber;

    public Token(String lexeme, String tokenType, int lineNumber, int columnNumber) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
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

    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public String toString() {
        return "Token{" +
                "lexeme='" + lexeme + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", line=" + lineNumber +
                ", col=" + columnNumber +
                '}';
    }

}
