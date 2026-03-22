package main.java.tokenizer;
import main.java.model.Token;
import java.util.ArrayList;
public class Tokenizer extends TokenRecognizer {
    // Main class for tokenization logic

    // Methods and attributes in this class are tentatively planned and may be subject to change as development progresses.

    // Needed attributes:
    // 1. List of tokens [private ArrayList<Token>]
    private ArrayList<Token> tokens;

    // Constructor to initialize tokens
    public Tokenizer() {
        this.tokens = new ArrayList<>();
    }

    // Needed methods:
    // 1. emptyTokens() - to clear list of tokens when needed [public void]
    public void emptyTokens() {
        tokens.clear();
    }
    // 2. tokenize(line of code, line number) - to analyze token per line using TokenRecognizer and add to list of tokens [public void]
     public void tokenize(String line, int lineNumber) {

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
            // create token object
            Token token = new Token(part, formattedType, lineNumber);
            // add to list
            tokens.add(token);
        }
    }
    // 3. getTokens() - return list of tokens [public ArrayList<Token>]
    public ArrayList<Token> getTokens() {
        return tokens;
    }
    // 4. toSentenceCase (token type) - to convert token type to sentence case for better readability {ex. data-type-keyword -> Data Type Keyword} [private String]
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

    // Added note:
    // For tokenize method, strip the line by spaces para maging array of string 
    // then loop through that array para isa isang ma-recognize ang token
}