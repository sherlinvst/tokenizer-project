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

    private ArrayList<String> splitLine (String parts){
        ArrayList<String> split = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < parts.length(); i++) {
            char c = parts.charAt(i);

            // for string literal
            if (c == '"') {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }

                StringBuilder str = new StringBuilder();
                str.append(c);
                i++;

                while (i < parts.length() && parts.charAt(i) != '"') {
                    str.append(parts.charAt(i));
                    i++;
                }

                if (i < parts.length()) {
                    str.append('"'); // closing quote
                }

                split.add(str.toString());
            }

            // for identifier and number
            else if (Character.isLetterOrDigit(c) || c == '_') {
                current.append(c);
            }

            // for symbols and operators
            else {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }

                if (i + 1 < parts.length()) {
                    char next = parts.charAt(i + 1);

                    if ((c == '=' && next == '=') ||
                        (c == '!' && next == '=') ||
                        (c == '<' && next == '=') ||
                        (c == '>' && next == '=') ||
                        (c == '&' && next == '&') ||
                        (c == '|' && next == '|')) {

                        split.add("" + c + next);
                        i++; // skip next char
                        continue;
                    }
                }

                if (!Character.isWhitespace(c)) {
                    split.add(String.valueOf(c));
                }
            }
        }

        // last token
        if (current.length() > 0) {
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