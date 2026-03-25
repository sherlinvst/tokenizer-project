package main.java.tokenizer;

import main.java.model.Token;
import java.util.ArrayList;

public class Tokenizer extends TokenRecognizer {
    private ArrayList<Token> tokens;

    // Constructor to initialize tokens
    public Tokenizer() {
        this.tokens = new ArrayList<>();
    }

    public void emptyTokens() {
        tokens.clear();
    }

     public void tokenize(String line) {
        // initialize if null (safety)
        if (tokens == null) {
            tokens = new ArrayList<>();
        }

        // strip the line by spaces para maging array of string 
        String[] parts = line.trim().split("\\s+");

        // then loop through that array para isa isang ma-recognize ang token
        for (String part : parts) {
            if (part.isEmpty()) continue;

            // use TokenRecognizer method
            String type = recognizeTokens(part);

            // convert to readable format
            String formattedType = toSentenceCase(type);
            
            // check if token already exist
            Token token = getToken(part);

            if (token == null){
                Token newToken = new Token(part, formattedType);
                tokens.add(newToken);
            }
            else {
                token.setOccurrence(token.getOccurrence() + 1);
            }          
        }
    }

    private Token getToken (String part){
        for (Token token : tokens){
          if (part.equals(token.getLexeme())) return token;
        }
        return null;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    private String toSentenceCase(String type) {

        if (type == null || type.isEmpty()) return "";

        String[] words = type.split("-");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }
}