package main.java.gui;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import main.java.gui.popups.*;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;

public class LexemizerFrame extends JFrame {

    //panel
    private CodePanel   codePanel;
    private ResultPanel lexemePanel;
    private ResultPanel occurrencePanel;
    private ResultPanel tokenPanel;
    private HeaderPanel headerPanel;
    private Tokenizer tokenizer;

    //mga color
    public static final Color BG_DARK   = new Color(0x0D0B1E);
    public static final Color PANEL_BG  = new Color(0x13102A);
    public static final Color FG_WHITE  = new Color(0xF0EEFF);
    public static final Color FG_DIM    = new Color(0x7A70A0);

    public LexemizerFrame() {
        super("Lexemizer");
        this.tokenizer = new Tokenizer();
        initWindow();
        init();
        layoutComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void init(){
        headerPanel = new HeaderPanel();
        codePanel = new CodePanel(this);
        lexemePanel = new ResultPanel("LEXEME");
        occurrencePanel = new ResultPanel("OCCURRENCE");
        tokenPanel = new ResultPanel("TOKEN");
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setPreferredSize(new Dimension(1350, 700));
        getContentPane().setBackground(FG_WHITE);
        setLayout(new BorderLayout(0, 0));
    }

    private void layoutComponents() {
        add(headerPanel, BorderLayout.NORTH);
        
        //sa spacing ng bawat panel 
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 7);

        //code panel
        gbc.weightx = 2.5;
        gbc.gridx = 0;
        wrapper.add(codePanel, gbc);

        //lexeme
        gbc.weightx = 1.5;
        gbc.gridx = 1;
        wrapper.add(lexemePanel, gbc);

        //occurrence
        gbc.weightx = 1.0;
        gbc.gridx = 2;
        wrapper.add(occurrencePanel, gbc);

        //token
        gbc.weightx = 2.0;
        gbc.gridx = 3;
        wrapper.add(tokenPanel, gbc);

        add(wrapper, BorderLayout.CENTER);
    }

    //pangtrial lang toh 
    public void onGetToken(String sourceCode) { 
        this.tokenizer.emptyTokens();

         String[] lines = sourceCode.split("\\s+\\r\\n|(?<=[;=(){}\\[\\]])|(?=[;=(){}\\[\\].])");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isBlank()) {
                tokenizer.tokenize(line);
            }
        }

        lexemePanel.clearRows();
        occurrencePanel.clearRows();
        tokenPanel.clearRows();

        List<Token> tokens = tokenizer.getTokens();
        for (Token t : tokens) {
            lexemePanel.addRow(t.getLexeme());
            occurrencePanel.addRow(String.valueOf(t.getOccurrence()));
            tokenPanel.addRow(t.getTokenType());
        }
    }

    //toh ren trail lang
    public void onClear() {
        lexemePanel.clearRows();
        tokenPanel.clearRows();
        tokenizer.emptyTokens();
        occurrencePanel.clearRows();
    }

    public void start() {
    setVisible(true);
    new WcPop(this).showPopup(); // ← triggers on launch
    }
}