package main.java.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;

public class SyntaxHighlighter {
    private final SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet operatorStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet literalStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet defaultStyle = new SimpleAttributeSet();

    public SyntaxHighlighter() {
        // Setup colors to match your aesthetic
        StyleConstants.setForeground(keywordStyle, new Color(0xFF6AD5)); // Pink
        StyleConstants.setBold(keywordStyle, true);
        
        StyleConstants.setForeground(operatorStyle, new Color(0xFFF35C)); // Yellow
        
        StyleConstants.setForeground(literalStyle, new Color(0x94FBAB)); // Green
        
        StyleConstants.setForeground(defaultStyle, Color.WHITE);
    }

    public void updateHighlighting(JTextPane pane) {
        String text = pane.getText();
        if (text.isEmpty()) return;

        // 1. Run your group's Tokenizer
        Tokenizer tokenizer = new Tokenizer();
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(text.split("\n", -1)));
        tokenizer.tokenizeLines(lines, 1);
        ArrayList<Token> tokens = tokenizer.getTokens(); // Ensure you have a getTokens() in Tokenizer.java

        StyledDocument doc = pane.getStyledDocument();
        Element root = doc.getDefaultRootElement();

        // 2. Highlighting logic
        SwingUtilities.invokeLater(() -> {
            // Reset to default white
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

            for (Token t : tokens) {
                if (t.getTokenType().equals("EOF")) continue;

                int line = t.getLineNumber() - 1;
                if (line >= root.getElementCount()) continue;

                // Calculate the 1D position Swing needs
                int startOffset = root.getElement(line).getStartOffset() + (t.getColumnNumber() - 1);
                int length = t.getLexeme().length();

                // Assign style based on the TokenType returned by your Tokenizer
                AttributeSet style = getStyle(t.getTokenType());
                doc.setCharacterAttributes(startOffset, length, style, false);
            }
        });
    }

    private AttributeSet getStyle(String type) {
        type = type.toLowerCase();
        if (type.contains("keyword")) return keywordStyle;
        if (type.contains("operator")) return operatorStyle;
        if (type.contains("literal")) return literalStyle;
        return defaultStyle;
    }
}