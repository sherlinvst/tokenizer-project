package main.java.tokenizer;

import main.java.model.Keywords;
import main.java.model.Operators;
import main.java.model.Separators;

public abstract class TokenRecognizer {
    private final String identifier = "Identifier";
    private final String literal = "Literal";
    private final String invalidInput = "Invalid Input";

    private final Keywords keywords = new Keywords();
    private final Operators operators = new Operators();
    private final Separators separators = new Separators();

    private enum LiteralType {
        STRING, CHAR, NUMERIC, FLOAT, BOOLEAN, SPECIAL
    }

    protected String recognizeTokens(String lexeme){
      // For more optimized version, check first if number or string
      // if number then automatic literal na sha, check type of literal
      // if string then check first enclosed ba ng ""
      // if enclosed ng "" then for sure literal na string
      // check if keyword, if not keyword then identifier sha
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
            return LiteralType.SPECIAL.toString().toLowerCase() + literal;
        }

        if(lexeme.equals("true") || lexeme.equals("false")){
             return LiteralType.BOOLEAN.toString().toLowerCase() + literal;
        }

        if(lexeme.matches("^\".*\"$")){
             return LiteralType.STRING.toString().toLowerCase() + literal;
        }

        if(lexeme.matches("^'.'$")){
             return LiteralType.CHAR.toString().toLowerCase() + literal;
        }

        if(lexeme.matches("^[+-]?\\d+\\.\\d+$")){
             return LiteralType.FLOAT.toString().toLowerCase() + literal;
        }

        if(lexeme.matches("^[+-]?\\d+$")){
             return LiteralType.NUMERIC.toString().toLowerCase() + literal;
        }

        return null;
    }

    private boolean isIdentifier(String lexeme){
        return lexeme.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}