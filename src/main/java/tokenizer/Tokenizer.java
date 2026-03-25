package main.java.tokenizer;

import main.java.model.Token;
import java.util.ArrayList;
import java.util.HashMap;

public class Tokenizer extends TokenRecognizer {
    private HashMap<String, Token> tokenMap;

    // Constructor to initialize tokens
    public Tokenizer() {
        this.tokenMap = new HashMap<>();
    }

    public void emptyTokens() {
        tokenMap.clear();
    }

     public void tokenize(String line) {
        // initialize if null (safety)
        if (tokenMap == null) {
            tokenMap = new HashMap<>();
        }

        // strip the line 
        ArrayList<String> parts = splitLine(line);

        // then loop through that array para isa isang ma-recognize ang token
        for (String part : parts) {
            if (part.isEmpty()) continue;

            // use TokenRecognizer method
            String type = recognizeTokens(part);

            // convert to readable format
            String formattedType = toSentenceCase(type);
            
            // check if token already exist
            if (!tokenMap.containsKey(part)) {
                Token newToken = new Token(part, formattedType);
                tokenMap.put(part, newToken);
            } else {
                Token token = tokenMap.get(part);
                token.setOccurrence(token.getOccurrence() + 1);
            }          
        }
    }

    private ArrayList<String> splitLine(String line) {
        ArrayList<String> split = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) continue;

            // Handle string literal
            if (c == '"') {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }

                StringBuilder str = new StringBuilder();
                str.append(c);
                i++;
                while (i < line.length() && line.charAt(i) != '"') {
                    str.append(line.charAt(i));
                    i++;
                }
                if (i < line.length()) str.append('"');
                split.add(str.toString());
                continue;
            }

            // for character literal
            else if (c == '\'') {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }

                StringBuilder charLit = new StringBuilder();
                charLit.append(c); // starting single quote
                i++;

                // collect everything until closing single quote
                while (i < line.length() && line.charAt(i) != '\'') {
                    charLit.append(line.charAt(i));
                    i++;
                }

                if (i < line.length()) {
                    charLit.append('\''); // closing single quote
                }

                split.add(charLit.toString());
                continue;
            }

            // Two-character operators
            if (i + 1 < line.length()) {
                char next = line.charAt(i + 1);
                if ((c == '=' && next == '=') || (c == '!' && next == '=') ||
                    (c == '<' && next == '=') || (c == '>' && next == '=') ||
                    (c == '&' && next == '&') || (c == '|' && next == '|')) {
                    split.add("" + c + next);
                    i++;
                    continue;
                }
            }

            // Start of identifier or invalid identifier
            current.setLength(0);
            current.append(c);
            i++;

            while (i < line.length()) {
                char next = line.charAt(i);
                // valid identifier chars
                if (Character.isLetterOrDigit(next) || next == '_') {
                    current.append(next);
                    i++;
                }
                // allow dash in both valid/invalid to capture full lexeme
                else if (next == '-') {
                    current.append(next);
                    i++;
                }
                else {
                    break;
                }
            }
            i--; // adjust index
            split.add(current.toString());
        }

        return split;
    }

    public ArrayList<Token> getTokens() {
        return new ArrayList<>(tokenMap.values());
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