package main.java.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import main.java.compiler.codegen.CodeGenerator;
import main.java.compiler.ir.IRInstruction;
import main.java.compiler.parser.ParseError;
import main.java.compiler.parser.Parser;
import main.java.compiler.parser.ast.ASTNode;
import main.java.compiler.semantic.SemanticError;
import main.java.gui.popups.*;
import main.java.tokenizer.Tokenizer;


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
        outputPanel.clearRows();

        Parser parser = new Parser(tokenizer.getTokens());
        ArrayList<ASTNode> ast = parser.parse();
        /*SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(ast);*/
        if(!parser.getErrors().isEmpty()) {
            outputPanel.addRow("=== COMPILATION FAILED ===\n");
            outputPanel.addRow("=== SYNTAX ERRORS ===\n");
            for (ParseError err : parser.getErrors()) {
                outputPanel.addRow(err.getMessage());
            } return;
        } 

        CodeGenerator generator = new CodeGenerator();
        String finalJavaCode = generator.generate(ast);

        if (generator.hasSemanticErrors()) {
            outputPanel.addRow("=== COMPILATION FAILED ===\n");
            outputPanel.addRow("=== SEMANTIC ERRORS ===\n");
            for (SemanticError err : generator.getSemanticErrors()) {
                outputPanel.addRow(err.getMessage());
            }
        } else {
                outputPanel.addRow("=== GENERATED IR ===\n");
                for (IRInstruction instr : generator.getIRGenerator().getInstructions()) {
                    outputPanel.addRow(instr.toString());
                }
                outputPanel.addRow("\n\n=== GENERATED JAVA CODE ===\n");
                outputPanel.addRow(finalJavaCode);
                outputPanel.addRow("\n\n=== COMPILATION SUCCESSFUL ===");
        }
    }

        /*if (parser.getErrors().isEmpty() && analyzer.getErrors().isEmpty()) {
            outputPanel.addRow("Compilation successful.");
        } else {
            outputPanel.addRow("Compilation failed.\n");

            for (ParseError err : parser.getErrors()) {
                outputPanel.addRow(err.getMessage());
            }
            
            outputPanel.addRow("\n");

            for (SemanticError err : analyzer.getErrors()) {
                outputPanel.addRow(err.getMessage());
            }
        }
    }*/

    public void onClear() {
        outputPanel.clearRows();
        tokenizer.emptyTokens();
    }

    public void start() {
    setVisible(true);
    new WcPop(this).showPopup(); // ← triggers on launch
    }
}