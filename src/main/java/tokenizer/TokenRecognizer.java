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

        if(lexeme == null || lexeme.isEmpty()) return invalidInput;

        // check if lexeme matches any literal type (not just number or string), then classify kung saan literal type magfall under 
        String literalToken = getLiteralType(lexeme);
        if(literalToken != null) return literalToken;

        // if not literal type, it will be check if kwywords or opertor or separator
        String keywordToken = keywords.getToken(lexeme);
        if(keywordToken != null) return keywordToken;

        String operatorToken = operators.getToken(lexeme);
        if(operatorToken != null) return operatorToken;

        String separatorToken = separators.getToken(lexeme);
        if(separatorToken != null) return separatorToken;

        // if wala sa token, then identifier sha
        if(isIdentifier(lexeme)) return identifier;

        return invalidInput;
    }

    private String getLiteralType(String lexeme){
        // special literal
        if(lexeme.equals("null")) return LiteralType.SPECIAL.toString().toLowerCase() + literal;
        // boolean literal
        if(lexeme.equals("true") || lexeme.equals("false")) return LiteralType.BOOLEAN.toString().toLowerCase() + literal;

        // string literal
        // used regex including if ang lexeme ay nagstart sa " and end with "
        // includes escape sequence like "\n", "\t"
        if(lexeme.matches("^\"(\\\\.|[^\"])*\"$")){
             return LiteralType.STRING.toString().toLowerCase() + literal;
        }

        // for char literal
        // used regex including if ang lexeme starts with ' and end with '
        // includes ecape sequence like '\t', '\n'
        if(lexeme.matches("^'(\\\\[btnfr'\"\\\\]|[^\\\\'])'$")){
             return LiteralType.CHAR.toString().toLowerCase() + literal;
        }
        
        // for float literal
        if(lexeme.matches("^[+-]?\\d*\\.\\d+$")){
             return LiteralType.FLOAT.toString().toLowerCase() + literal;
        }

        //for numeric literal
        if( lexeme.matches("^0[xX][0-9a-fA-F]+$") || // if hex 
            lexeme.matches("^0[bB][01]+$") || // if binary
            lexeme.matches("^0[0-7]+$") || // if octal
            lexeme.matches("^[+-]?\\d+$")){ // if standard
                return LiteralType.NUMERIC.toString().toLowerCase() + literal;
        }

        return null;
    }

    private boolean isIdentifier(String lexeme){
        return lexeme.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
    
}
