package main.java.tokenizer;

import java.util.ArrayList;
import main.java.model.Token;

public class Tokenizer extends TokenRecognizer {
    // temp class object to store token and column number
    private static class RawToken {
        final String text;
        final int col; // 1-based column where this token starts

        RawToken(String text, int col) {
            this.text = text;
            this.col = col;
        }
    }

    private ArrayList<Token> tokens;
    private boolean inBlockComment = false;

    // Constructor to initialize tokens
    public Tokenizer() {
        this.tokens = new ArrayList<>();
    }

    public void emptyTokens() {
        tokens.clear();
    }

    // call this function to input ALL lines of code and put the line number of the first line that is NOT EMPTY
    public void tokenizeLines (ArrayList<String> lines, int lineNumber) {
        for (String line : lines) {
            tokenize(line, lineNumber);
            lineNumber++;
        }
        tokens.add(new Token("EOF", "EOF", lines.size() + 1, 0));
    }

    private void tokenize(String line, int lineNumber) {
        // skip single line comments
        String trimmed = line.trim();

        if (trimmed.startsWith("//")) {
            return;
        }

        // inline comments
        int commentIndex = line.indexOf("//");
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        ArrayList<RawToken> parts = splitLine(line);

        for (RawToken raw : parts) {
            if (raw.text.isEmpty()) continue;

            String type = recognizeTokens(raw.text);
            String formattedType = toSentenceCase(type);

            tokens.add(new Token(raw.text, formattedType, lineNumber, raw.col));
        }
    }
    private ArrayList<RawToken> splitLine(String line) {
        ArrayList<RawToken> split = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int startCol = 1;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inBlockComment) {
                if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++; // skip '/'
                }
                continue;
            }

            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
                inBlockComment = true;
                i++; // skip '*'
                continue;
            }

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                if (current.length() > 0) {
                    split.add(new RawToken(current.toString(), startCol));
                    current.setLength(0);
                }
                continue;
            }
            
            if (current.length() == 0) {
                startCol = i + 1; // i is 0-based, col is 1-based
            }

            // Handle string literal
            if (c == '"') {
                if (current.length() > 0) {
                    split.add(new RawToken(current.toString(), startCol));
                    current.setLength(0);
                }
                int strStart = i + 1; // 1-based col of the opening quote
                StringBuilder str = new StringBuilder();
                str.append(c);
                i++;
                while (i < line.length() && line.charAt(i) != '"') {
                    str.append(line.charAt(i));
                    i++;
                }
                if (i < line.length()) str.append('"');
                split.add(new RawToken(str.toString(), strStart));
                continue;
            }

            // Handle character literal
            if (c == '\'') {
                if (current.length() > 0) {
                    split.add(new RawToken(current.toString(), startCol));
                    current.setLength(0);
                }
                int charStart = i + 1;
                StringBuilder charLit = new StringBuilder();
                charLit.append(c);
                i++;
                while (i < line.length() && line.charAt(i) != '\'') {
                    charLit.append(line.charAt(i));
                    i++;
                }
                if (i < line.length()) charLit.append('\'');
                split.add(new RawToken(charLit.toString(), charStart));
                continue;
            }

            // Two-character operators
            if (i + 1 < line.length()) {
                char next = line.charAt(i + 1);
                if ((c == '=' && next == '=') || (c == '!' && next == '=') ||
                    (c == '<' && next == '=') || (c == '>' && next == '=') ||
                    (c == '&' && next == '&') || (c == '|' && next == '|') ||
                    (c == '+' && next == '+') || (c == '-' && next == '-') ||
                    (c == '+' && next == '=') || (c == '-' && next == '=') ||
                    (c == '*' && next == '=') || (c == '/' && next == '=') ||
                    (c == '%' && next == '=') || (c == '<' && next == '<') || (c == '>' && next == '>')){
                    if (current.length() > 0) {
                        split.add(new RawToken(current.toString(), startCol));
                        current.setLength(0);
                    }
                    split.add(new RawToken("" + c + next, i + 1));
                    i++;
                    continue;
                }
            }

            // Handle period
            if (c == '.') {
                boolean nextIsDigit = (i + 1 < line.length()) && Character.isDigit(line.charAt(i + 1));

                if (isNumeric(current) && nextIsDigit) {
                    // Appending — even if current already has a dot
                    // Flag it as Syntax Error
                    current.append(c);
                } else if (current.length() == 0 && nextIsDigit) {
                    // Leading decimal
                    current.append(c);
                } else {
                    // Standalone dot for method chain, member access
                    if (current.length() > 0) {
                        split.add(new RawToken(current.toString(), startCol));
                        current.setLength(0);
                    }
                    split.add(new RawToken(".", i + 1));
                }
                continue;
            }

            // Single-character operators/delimiters
            if (";,+-*/%=<>&|^~(){}[]".indexOf(c) != -1) {

                // Catch sign after 'e'/'E' in scientific notation
                if ((c == '+' || c == '-')
                        && current.length() > 0
                        && (current.charAt(current.length() - 1) == 'e'
                            || current.charAt(current.length() - 1) == 'E')) {
                    current.append(c); // part of scientific notation
                    continue;
                }

                if (current.length() > 0) {
                    split.add(new RawToken(current.toString(), startCol));
                    current.setLength(0);
                }

                // Check if + or - is a sign for a number, not an operator
                if ((c == '+' || c == '-')) {
                    boolean nextIsDigit = (i + 1 < line.length()) && Character.isDigit(line.charAt(i + 1));
                    boolean nextIsDot   = (i + 1 < line.length()) && line.charAt(i + 1) == '.'
                                          && (i + 2 < line.length()) && Character.isDigit(line.charAt(i + 2));
                    boolean isSigned = (split.isEmpty() || isOperatorOrDelimiter(split.get(split.size() - 1).text));

                    if (isSigned && (nextIsDigit || nextIsDot)) {
                      startCol = i + 1;
                        current.append(c);
                        continue;
                    }
                }

                split.add(new RawToken("" + c, i + 1));
                continue;
            }

            // Build identifier or number character by character
            // Allow float/double letters (f, F, d, D) at end of numeric token
            if ((c == 'f' || c == 'F' || c == 'd' || c == 'D')
                    && current.length() > 0
                    && isNumeric(current)) {
                current.append(c);
                split.add(new RawToken(current.toString(), startCol));
                current.setLength(0);
            }
            // For scientific notation
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

        // For any remaining token
        if (current.length() > 0) {
            split.add(new RawToken(current.toString(), startCol));
        }

        return split;
    }

      private boolean isOperatorOrDelimiter(String s) {
          if (s == null || s.isEmpty()) return false;
          String[] ops = {
              "+", "-", "*", "/", "%",
              "=", "<", ">","!","&", 
              "|", "^", "~",">>>",
              "(", ")", "{", "}", "[", "]",
              ":",";", ",", "."
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