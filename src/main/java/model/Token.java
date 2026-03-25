package main.java.model;

public class Token {
    private final String lexeme; // The actual string representation of the token inputted by the user
    private final String tokenType; // Must be in title case format
    private int occurrence; // Number of lexeme occurrence

    public Token(String lexeme, String tokenType) {
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.occurrence = 1;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int ctr){
        occurrence = ctr;
    }
}
