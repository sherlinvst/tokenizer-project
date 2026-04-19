package main.java.gui;

import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.java.gui.popups.*;
import main.java.model.Token;
import main.java.tokenizer.Tokenizer;
import main.java.compiler.parser.Parser;
import main.java.compiler.parser.ParseError;
import main.java.compiler.parser.ast.ASTNode;

public class LexemizerFrame extends JFrame {

    //panel
    private CodePanel codePanel;
    private ResultPanel outputPanel;
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
        outputPanel = new ResultPanel("OUTPUT");
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

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);

        gbc.weightx = 1.0;
        gbc.gridx = 0;
        wrapper.add(codePanel, gbc);

        gbc.weightx = 1.0;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        wrapper.add(outputPanel, gbc);

        add(wrapper, BorderLayout.CENTER);
    }

    public void onGetToken(String sourceCode) {
        tokenizer.emptyTokens();

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(sourceCode.split("\n", -1)));

        int firstLine = 1;
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                firstLine = i + 1;
                break;
            }
        }

        tokenizer.tokenizeLines(lines, firstLine);
<<<<<<< ours
        List<Token> tokens = tokenizer.getTokens();

=======
        // Parser parser = new Parser(tokenizer.getTokens());
        // ArrayList<ASTNode> ast = parser.parse();

        // System.out.println("Parse successful. " + ast.size() + " top-level nodes.");
        
        // pang-display ng errors
        // if (parser.getErrors().isEmpty()) {
        //     outputPanel.addRow("No parse errors found.");
        // } else {
        //    for (ParseError error : parser.getErrors()) {
        //        outputPanel.addRow(error.getMessage());
        //    }
        // }
>>>>>>> theirs
        outputPanel.clearRows();

        Parser parser = new Parser(tokens);
        ArrayList<ASTNode> ast = parser.parse();

        if (parser.getErrors().isEmpty()) {
            outputPanel.addRow("Compilation successful.");
            outputPanel.addRow("AST Nodes: " + ast.size());
        } else {
            outputPanel.addRow("Compilation failed.\n");

            for (ParseError err : parser.getErrors()) {
                outputPanel.addRow(err.getMessage());
            }
        }
    }

    public void onClear() {
        outputPanel.clearRows();
        tokenizer.emptyTokens();
    }

    public void start() {
    setVisible(true);
    new WcPop(this).showPopup(); // ← triggers on launch
    }
}