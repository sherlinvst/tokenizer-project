package main.java.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import main.java.gui.popups.WarnPop;

public class CodePanel extends RoundedPanel {

    private  JLabel lineCountLabel;
    private  JTextArea codeArea;
    private  JTextArea lineNumbers;

    private final LexemizerFrame frame;

    private static final Font MONO  = new Font("Courier New", Font.PLAIN, 14);
    private static final Font LABEL = new Font("Consolas", Font.BOLD, 13);

    public CodePanel(LexemizerFrame frame) {
        super(16);
        this.frame = frame;  
        init();
    }
    
    private void init() {
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(0, 0, 0, 0));

        lineCountLabel = buildLineCountLabel();
        codeArea       = buildCodeArea();
        lineNumbers    = buildLineNumberArea();

        layoutComponents();
        attachListeners();
        updateLineCount();
    }

    private JLabel buildLineCountLabel() {
        JLabel lbl = new JLabel("Number of lines: 1", SwingConstants.CENTER);
        lbl.setFont(LABEL);
        lbl.setForeground(LexemizerFrame.FG_WHITE);
        lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
        lbl.setOpaque(false);
        return lbl;
    }

    private JTextArea buildCodeArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(MONO);
        ta.setBackground(LexemizerFrame.PANEL_BG);
        ta.setForeground(LexemizerFrame.FG_WHITE);
        ta.setCaretColor(LexemizerFrame.FG_WHITE);
        ta.setSelectionColor(new Color(0x3D2B6E));
        ta.setBorder(new EmptyBorder(4, 8, 4, 8));
        ta.setLineWrap(false);
        ta.setTabSize(4);
        return ta;
    }

    private JTextArea buildLineNumberArea() {
        JTextArea ln = new JTextArea("1");
        ln.setFont(MONO);
        ln.setBackground(LexemizerFrame.PANEL_BG);
        ln.setForeground(LexemizerFrame.FG_DIM);
        ln.setBorder(new EmptyBorder(4, 10, 4, 6));
        ln.setEditable(false);
        ln.setFocusable(false);
        return ln;
    }

    private void layoutComponents() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lineCountLabel, BorderLayout.CENTER);

        //yung horizontal line separator sa baba ng header
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2A2550));
        top.add(sep, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        //scroll together with line numbers
        JScrollPane scroll = new JScrollPane(codeArea);
        scroll.setRowHeaderView(lineNumbers);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(LexemizerFrame.PANEL_BG);
        scroll.setBackground(LexemizerFrame.PANEL_BG);
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        add(scroll, BorderLayout.CENTER);

        //buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnRow.setOpaque(false);

        JButton clearBtn    = buildButton("CLEAR",false);
        JButton getTokenBtn = buildButton("GET TOKEN", true);

        btnRow.add(clearBtn);

        JPanel rightAlign = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        rightAlign.setOpaque(false);
        rightAlign.add(getTokenBtn);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(btnRow,     BorderLayout.WEST);
        bottomRow.add(rightAlign, BorderLayout.EAST);

        add(bottomRow, BorderLayout.SOUTH);

        //action ng buttons clear and get token
        clearBtn.addActionListener((ActionEvent e) -> {
            codeArea.setText("");
            frame.onClear();
        });

        getTokenBtn.addActionListener((ActionEvent e) -> {
            String content = codeArea.getText().trim(); //to avoid including spaces in counting tokens
            if (content.isEmpty()) {
                new WarnPop(frame).showPopup();
            } else {
                frame.onGetToken(content);
            }
        });
    }

    //style ng buttons like if clinick mo magbabago color
    private JButton buildButton(String text, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (filled) {
                    g2.setColor(getModel().isPressed()
                            ? new Color(0x8B5CF6)   
                            : new Color(0xFFFFFF)); 
                } else {
                    g2.setColor(getModel().isPressed()
                            ? new Color(0x8B5CF6)
                            : new Color(0xFFFFFF));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!filled) {
                    g2.setColor(LexemizerFrame.FG_WHITE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(LexemizerFrame.BG_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    //style para sa scroll bar like tinaggal ko yung up and down arrow para aesthetic
    private void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(80, 70, 140);  
                trackColor = new Color(42, 37, 80); 
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroButton(); 
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroButton(); 
            }

            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });

        scrollBar.setUnitIncrement(16);
    }

    //para sa toh sa number line like para sync sya kapag nag new newline
    private void attachListeners() {
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updateLineCount(); }
            public void removeUpdate(DocumentEvent e)  { updateLineCount(); }
            public void changedUpdate(DocumentEvent e) { updateLineCount(); }
        });
    }

    private void updateLineCount() {
        int lines = codeArea.getLineCount();
        lineCountLabel.setText("Number of lines: " + lines);
        updateLineNumbers(lines);
    }

    private void updateLineNumbers(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            sb.append(i);
            if (i < count) sb.append('\n');
        }
        lineNumbers.setText(sb.toString());
    }
    //testing lang toh
    public String getSourceCode() {
        return codeArea.getText();
    }
}
