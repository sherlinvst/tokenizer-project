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

            // Skip whitespace — flush current first
            if (Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

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

            // Handle character literal
            if (c == '\'') {
                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }
                StringBuilder charLit = new StringBuilder();
                charLit.append(c);
                i++;
                while (i < line.length() && line.charAt(i) != '\'') {
                    charLit.append(line.charAt(i));
                    i++;
                }
                if (i < line.length()) charLit.append('\'');
                split.add(charLit.toString());
                continue;
            }

            // Two-character operators
            if (i + 1 < line.length()) {
                char next = line.charAt(i + 1);
                if ((c == '=' && next == '=') || (c == '!' && next == '=') ||
                    (c == '<' && next == '=') || (c == '>' && next == '=') ||
                    (c == '&' && next == '&') || (c == '|' && next == '|')) {
                    if (current.length() > 0) {
                        split.add(current.toString());
                        current.setLength(0);
                    }
                    split.add("" + c + next);
                    i++;
                    continue;
                }
            }

            // Handle period
            if (c == '.') {
                boolean nextIsDigit = (i + 1 < line.length()) && Character.isDigit(line.charAt(i + 1));

                if (isNumeric(current) && nextIsDigit) {
                    // Keep appending — even if current already has a dot (e.g. "3.14" + "." + "15" = "3.14.15")
                    // This lets TokenRecognizer flag it as Syntax Error
                    current.append(c);
                } else if (current.length() == 0 && nextIsDigit) {
                    // Leading decimal e.g. ".3"
                    current.append(c);
                } else {
                    // Standalone dot: method chain, member access
                    if (current.length() > 0) {
                        split.add(current.toString());
                        current.setLength(0);
                    }
                    split.add(".");
                }
                continue;
            }

            // Single-character operators/delimiters
            if (";,+-*/%=<>&|^~(){}[]".indexOf(c) != -1) {

                // Catch sign after 'e'/'E' in scientific notation BEFORE flushing
                if ((c == '+' || c == '-')
                        && current.length() > 0
                        && (current.charAt(current.length() - 1) == 'e'
                            || current.charAt(current.length() - 1) == 'E')) {
                    current.append(c); // part of scientific notation, e.g. "1e-"
                    continue;
                }

                if (current.length() > 0) {
                    split.add(current.toString());
                    current.setLength(0);
                }

                // Check if + or - is a sign for a number, not an operator
                if ((c == '+' || c == '-')) {
                    boolean nextIsDigit = (i + 1 < line.length()) && Character.isDigit(line.charAt(i + 1));
                    boolean nextIsDot   = (i + 1 < line.length()) && line.charAt(i + 1) == '.'
                                          && (i + 2 < line.length()) && Character.isDigit(line.charAt(i + 2));
                    boolean isSigned = (split.isEmpty() || isOperatorOrDelimiter(split.get(split.size() - 1)));

                    if (isSigned && (nextIsDigit || nextIsDot)) {
                        current.append(c);
                        continue;
                    }
                }

                split.add("" + c);
                continue;
            }

            // Build identifier or number character by character
            // Allow float/double suffix (f, F, d, D) at end of numeric token
            if ((c == 'f' || c == 'F' || c == 'd' || c == 'D')
                    && current.length() > 0
                    && isNumeric(current)) {
                current.append(c);
                split.add(current.toString());
                current.setLength(0);
            }
            // Scientific notation: e.g. "1e-10", "3.14e+2"
            // Catch 'e' or 'E' after a number
            else if ((c == 'e' || c == 'E')
                    && current.length() > 0
                    && isNumeric(current)) {
                current.append(c); // attach 'e', keep building
            }
            // Catch the sign after 'e'/'E' in scientific notation: "1e-" or "1e+"
            else if ((c == '+' || c == '-')
                    && current.length() > 0
                    && (current.charAt(current.length() - 1) == 'e'
                        || current.charAt(current.length() - 1) == 'E')) {
                current.append(c); // attach sign after exponent marker
            }
            else {
                current.append(c);
            }
        }

        // Flush any remaining token
        if (current.length() > 0) {
            split.add(current.toString());
        }

        return split;
    }

      private boolean isOperatorOrDelimiter(String s) {
          if (s == null || s.isEmpty()) return false;
          String[] ops = {
              "+", "-", "*", "/", "%", "=", "==", "!=", "<", ">", "<=", ">=",
              "&&", "||", "&", "|", "^", "~", "(", ")", "{", "}", "[", "]",
              ";", ",", "."
          };
          for (String op : ops) {
              if (s.equals(op)) return true;
          }
          return false;
      }

    // checks if buffer is a complete or partial numeric literal (int or decimal, with optional sign)
    private boolean isNumeric(StringBuilder sb) {
        if (sb.length() == 0) return false;
        int start = 0;
        if (sb.charAt(0) == '+' || sb.charAt(0) == '-') start = 1;
        if (start >= sb.length()) return false;
        boolean hasDot = false;
        for (int j = start; j < sb.length(); j++) {
            char ch = sb.charAt(j);
            if (ch == '.') {
                if (hasDot) return false; // two dots = not numeric
                hasDot = true;
            } else if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
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