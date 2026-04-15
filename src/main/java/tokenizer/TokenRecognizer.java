package main.java.tokenizer;

import main.java.model.Keywords;
import main.java.model.Operators;
import main.java.model.Separators;

public abstract class TokenRecognizer {
    private final String identifier = "Identifier";
    private final String literal = "-Literal";
    private final String invalidInput = "Syntax Error";

    private final Keywords keywords = new Keywords();
    private final Operators operators = new Operators();
    private final Separators separators = new Separators();

    private enum LiteralType {
        STRING, CHAR, NUMERIC, FLOAT, BOOLEAN, SPECIAL
    }

    protected String recognizeTokens(String lexeme){
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
        if(lexeme.equals("null")) return LiteralType.SPECIAL.toString().toLowerCase() + literal; // special literal
        if(lexeme.equals("true") || lexeme.equals("false")) return LiteralType.BOOLEAN.toString().toLowerCase() + literal; // boolean literal

        // string literal
        // if (lexeme.matches( "\"(?:[^\"\\\\\\n\\r]" + "|\\\\[abfnrtv\"'\\\\]" + "|\\\\u[0-9a-fA-F]{4}" +   "|\\\\[0-3]?[0-7]{1,2}" +   ")*\"")) {
        //     return LiteralType.STRING.toString().toLowerCase() + literal;
        // }
        if(isStringLiteral(lexeme)) return LiteralType.STRING.toString().toLowerCase() + literal;

        // for char literal
        // if(lexeme.matches("^'(?:[^\\\\'\\n\\r]|\\\\[abfnrtv\\\\'\"]|\\\\[0-7]{1,3}|\\\\x[0-9A-Fa-f]+|\\\\u[0-9A-Fa-f]{4}|\\\\U[0-9A-Fa-f]{8})'$")){
        //         return LiteralType.CHAR.toString().toLowerCase() + literal;
        // } 
        if(isCharLiteral(lexeme)) return LiteralType.CHAR.toString().toLowerCase() + literal;
                
        // float literal
        if (lexeme.matches("^[+-]?(?:\\d+\\.\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?[fFdD]?$"
                + "|^[+-]?\\d+[eE][+-]?\\d+[fFdD]?$"
                + "|^[+-]?\\d+[fFdD]$")) {
            return LiteralType.FLOAT.toString().toLowerCase() + literal;
        }

        // numeric literal
        if (
            lexeme.matches("^0[xX][0-9a-fA-F]+[lL]?$") || // hex 
            lexeme.matches("^0[bB][01]+[lL]?$") ||        // binary 
            lexeme.matches("^0[0-7]+[lL]?$") ||           // octal 
            lexeme.matches("^[+-]?\\d+[lL]?$")            // decimal 
        ) {
            return LiteralType.NUMERIC.toString().toLowerCase() + literal;
        }
        return null;
    }

    private boolean isStringLiteral(String lexeme) {
        // must start and end with double quotes
        if (lexeme.length() < 2 || lexeme.charAt(0) != '"' || lexeme.charAt(lexeme.length() - 1) != '"') {
            return false;
        }

        for (int i = 1; i < lexeme.length() - 1; i++) {
            char c = lexeme.charAt(i);

            if (c == '\\') {
                if (i + 1 >= lexeme.length() - 1) return false;
                char next = lexeme.charAt(i + 1);

                // Standard escapes
                if (next == 'a' || next == 'b' || next == 'f' || next == 'n' ||
                    next == 'r' || next == 't' || next == 'v' ||
                    next == '"' || next == '\'' || next == '\\') {
                    i++;
                }

                // Unicode: 
                else if (next == 'u') {
                    if (i + 5 >= lexeme.length() - 1) return false;
                    String hex = lexeme.substring(i + 2, i + 6);
                    for (char h : hex.toCharArray()) {
                        if (Character.digit(h, 16) == -1) return false;
                    }
                    i += 5;
                }

                // Hex \xAB...
                else if (next == 'x') {
                    int j = i + 2;
                    if (j >= lexeme.length() - 1) return false;
                    while (j < lexeme.length() - 1 && Character.digit(lexeme.charAt(j), 16) != -1) {
                        j++;
                    }
                    if (j == i + 2) return false; // must have at least one hex digit
                    i = j - 1;
                }

                // Octal: \7, \12, \377
                else if (next >= '0' && next <= '7') {
                    int j = i + 1;
                    if (j + 2 < lexeme.length() - 1 &&
                        lexeme.charAt(j) >= '0' && lexeme.charAt(j) <= '3' &&
                        lexeme.charAt(j + 1) >= '0' && lexeme.charAt(j + 1) <= '7' &&
                        lexeme.charAt(j + 2) >= '0' && lexeme.charAt(j + 2) <= '7') {
                        i += 3;
                    } else if (j + 1 < lexeme.length() - 1 &&
                            lexeme.charAt(j + 1) >= '0' && lexeme.charAt(j + 1) <= '7') {
                        i += 2;
                    } else {
                        i += 1;
                    }
                }

                else {
                    return false;
                }
            }
            else if (c == '\n' || c == '\r') {
                // raw newlines not allowed inside string literal
                return false;
            }
        }

        return true;
    }

    private boolean isCharLiteral(String lexeme) {
        // must start and end with single quotes
        if (lexeme.length() < 3 || lexeme.charAt(0) != '\'' || lexeme.charAt(lexeme.length() - 1) != '\'') {
            return false;
        }

        // normal char like 'a'
        if (lexeme.length() == 3) {
            char c = lexeme.charAt(1);
            return c != '\'' && c != '\\';
        }

        // escape sequences like '\n', '\t', '\\', '\''
        if (lexeme.length() == 4 && lexeme.charAt(1) == '\\') {
            char esc = lexeme.charAt(2);
            return "afbfnrtv'\"\\ ".indexOf(esc) >= 0;
        }

        // unicode 
        if (lexeme.startsWith("'\\u") && lexeme.length() == 8) {
            String hex = lexeme.substring(3, 7);
            return hex.chars().allMatch(ch -> Character.digit(ch, 16) != -1);
        }

        // octal '\7', '\12', '\377'
        if (lexeme.charAt(1) == '\\' && lexeme.length() >= 4 && lexeme.length() <= 6) {
            String oct = lexeme.substring(2, lexeme.length() - 1);
            return oct.matches("[0-7]{1,3}");
        }

        return false;
    }


    // check if lexeme is a valid identifier
    private boolean isIdentifier(String lexeme){
        return lexeme.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}