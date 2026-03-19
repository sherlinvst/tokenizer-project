package main.java.tokenizer;

public class Tokenizer extends TokenRecognizer {
    // Main class for tokenization logic

    // Methods and attributes in this class are tentatively planned and may be subject to change as development progresses.

    // Needed attributes:
    // 1. List of tokens [private ArrayList<Token>]

    // Needed methods:
    // 1. emptyTokens() - to clear list of tokens when needed [public void]
    // 2. tokenize(line of code, line number) - to analyze token per line using TokenRecognizer and add to list of tokens [public void]
    // 3. getTokens() - return list of tokens [public ArrayList<Token>]
    // 4. toSentenceCase (token type) - to convert token type to sentence case for better readability {ex. data-type-keyword -> Data Type Keyword} [private String]

    // Added note:
    // For tokenize method, strip the line by spaces para maging array of string 
    // then loop through that array para isa isang ma-recognize ang token
}
