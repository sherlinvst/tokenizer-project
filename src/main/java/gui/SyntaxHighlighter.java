package main.java.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;

public class SyntaxHighlighter {
    // Style Definitions
    private final SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet operatorStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet literalStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet commentStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet typeStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet methodStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet defaultStyle = new SimpleAttributeSet();

    public SyntaxHighlighter() {
        // Keywords: Pink
        StyleConstants.setForeground(keywordStyle, new Color(0xFF6AD5));
        StyleConstants.setBold(keywordStyle, true);
        
        // Operators: Yellow
        StyleConstants.setForeground(operatorStyle, new Color(0xFFF35C));
        
        // Literals: Green
        StyleConstants.setForeground(literalStyle, new Color(0x94FBAB));
        
        // Comments: Dim Purple/Gray
        StyleConstants.setForeground(commentStyle, new Color(0x7A70A0));
        StyleConstants.setItalic(commentStyle, true);

        // Types/Classes: Cyan
        StyleConstants.setForeground(typeStyle, new Color(0x8BE9FD));

        // Methods: Bright Green
        StyleConstants.setForeground(methodStyle, new Color(0x50FA7B));
        
        // Default: White
        StyleConstants.setForeground(defaultStyle, Color.WHITE);
    }

    public void updateHighlighting(JTextPane pane) {
        String text = pane.getText();
        if (text.isEmpty()) return;

        // 1. Run the Tokenizer
        Tokenizer tokenizer = new Tokenizer();
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens();

        StyledDocument doc = pane.getStyledDocument();
        Element root = doc.getDefaultRootElement();

        // 2. Apply highlighting on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Reset the entire document to default first
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                if (t.getTokenType().equals("EOF")) continue;

                int line = t.getLineNumber() - 1;
                if (line >= root.getElementCount()) continue;

                int startOffset = root.getElement(line).getStartOffset() + (t.getColumnNumber() - 1);
                int length = t.getLexeme().length();

                // 3. Contextual Style Selection
                AttributeSet style = getContextualStyle(tokens, i);
                doc.setCharacterAttributes(startOffset, length, style, false);
            }
            highlightLineComments(text, doc, root);
        });
    }

    private AttributeSet getContextualStyle(ArrayList<Token> tokens, int index) {
        Token t = tokens.get(index);
        String type = t.getTokenType().toLowerCase();
        String lexeme = t.getLexeme();

        // Standard matches
        if (type.contains("keyword")) return keywordStyle;
        if (type.contains("operator")) return operatorStyle;
        if (type.contains("literal")) return literalStyle;
        if (type.contains("comment")) return commentStyle;

        // Structural refinement for Identifiers
        if (type.contains("identifier")) {
            // If the next token is '(', it's a method call/declaration
            if (index + 1 < tokens.size() && tokens.get(index + 1).getLexeme().equals("(")) {
                return methodStyle;
            }
            // If it starts with an uppercase letter, treat it as a Class/Type
            if (Character.isUpperCase(lexeme.charAt(0))) {
                return typeStyle;
            }
        }

        return defaultStyle;
    }

    private void highlightLineComments(String text, StyledDocument doc, Element root) {
        String[] lines = text.split("\n", -1);

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];

            int commentIndex = line.indexOf("//");
            if (commentIndex == -1) continue;

            if (lineIndex >= root.getElementCount()) continue;

            int startOffset = root.getElement(lineIndex).getStartOffset() + commentIndex;
            int length = line.length() - commentIndex;

            doc.setCharacterAttributes(startOffset, length, commentStyle, false);
        }
    }
}