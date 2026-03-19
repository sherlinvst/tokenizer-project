package main.java.gui;

import javax.swing.*;
import java.awt.*;

public class LexemizerFrame extends JFrame {

    //panel
    private  CodePanel   codePanel;
    private  ResultPanel lexemePanel;
    private  ResultPanel tokenPanel;
    private  HeaderPanel headerPanel;

    //mga color
    public static final Color BG_DARK   = new Color(0x0D0B1E);
    public static final Color PANEL_BG  = new Color(0x13102A);
    public static final Color FG_WHITE  = new Color(0xF0EEFF);
    public static final Color FG_DIM    = new Color(0x7A70A0);

    public LexemizerFrame() {
        super("Lexemizer");
        initWindow();
        init();
        layoutComponents();
        pack();
        setLocationRelativeTo(null);
    }

    private void init(){
        headerPanel  = new HeaderPanel();
        codePanel    = new CodePanel(this);
        lexemePanel  = new ResultPanel("LEXEME");
        tokenPanel   = new ResultPanel("TOKEN");
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 650));
        setPreferredSize(new Dimension(1150, 700));
        getContentPane().setBackground(FG_WHITE);
        setLayout(new BorderLayout(0, 0));
    }

    private void layoutComponents() {
        add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 3, 14, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        content.add(codePanel);//this part is yung nilalagayn ng code    
        content.add(lexemePanel);// tong part is yung sa lexeme
        content.add(tokenPanel); //then eto yung a token
        
        //sa spacing ng bawat panel 
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 7);

        gbc.weightx = 2.0;
        gbc.gridx = 0;
        wrapper.add(codePanel, gbc);

        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 7, 0, 7);
        gbc.gridx = 1;
        wrapper.add(lexemePanel, gbc);

        gbc.insets = new Insets(0, 7, 0, 0);
        gbc.gridx = 2;
        wrapper.add(tokenPanel, gbc);

        add(wrapper, BorderLayout.CENTER);
    }

    //pangtrial lang toh 
    public void onGetToken(String sourceCode) { 
        lexemePanel.clearRows();
        tokenPanel.clearRows();
        String[] rawLexemes = sourceCode.trim().split("\\s+|(?<=[;=(){}\\[\\]])|(?=[;=(){}\\[\\]])");
        for (String lex : rawLexemes) {
            if (!lex.isBlank()) {
                lexemePanel.addRow(lex);
                tokenPanel.addRow("—"); 
            }
        }
    }
    //toh ren trail lang
    public void onClear() {
        lexemePanel.clearRows();
        tokenPanel.clearRows();
    }
}