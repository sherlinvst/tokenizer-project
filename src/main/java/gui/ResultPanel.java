package main.java.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultPanel extends RoundedPanel {
    private final String title;
    private final List<String> rows = new ArrayList<>();

    private JLabel headerLabel;
    private JPanel rowContainer;
    private JScrollPane scrollPane;

    private static final Font HEADER_FONT = new Font("Courier New", Font.BOLD,  13);
    private static final Font ROW_FONT    = new Font("Courier New", Font.PLAIN, 13);

    public ResultPanel(String title) {
        super(16);
        this.title = title;
        init();
        layoutComponents();
    }

    private void init(){
        setLayout(new BorderLayout());
        headerLabel = buildHeader();
        rowContainer = new JPanel();
        scrollPane = buildScrollPane();
    }

    private JLabel buildHeader() {
        JLabel lbl = new JLabel(title, SwingConstants.LEFT); 
        lbl.setFont(HEADER_FONT);
        lbl.setForeground(LexemizerFrame.FG_WHITE);
        lbl.setBorder(new EmptyBorder(10, 16, 10, 12));
        lbl.setOpaque(false);
        return lbl;
    }

    private JScrollPane buildScrollPane() {
        rowContainer.setLayout(new BoxLayout(rowContainer, BoxLayout.Y_AXIS));
        rowContainer.setOpaque(false);
        rowContainer.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane sp = new JScrollPane(rowContainer);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(LexemizerFrame.PANEL_BG);
        sp.setBackground(LexemizerFrame.PANEL_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        styleScrollBar(sp.getVerticalScrollBar());
        styleScrollBar(sp.createHorizontalScrollBar());

        return sp;
    }

    private void layoutComponents() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(headerLabel, BorderLayout.WEST);

        // CLEAR button on the right side of output header
        JButton clearBtn = buildClearButton();
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        btnWrapper.setOpaque(false);
        btnWrapper.add(clearBtn);
        top.add(btnWrapper, BorderLayout.EAST);

        clearBtn.addActionListener(e -> clearRows());

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2A2550));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(top, BorderLayout.CENTER);
        topWrapper.add(sep, BorderLayout.SOUTH);

        add(topWrapper,  BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
    }

    private JButton buildClearButton() {
        JButton btn = new JButton("CLEAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0x8B5CF6) : new Color(0xFFFFFF));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
        btn.setPreferredSize(new Dimension(100, 37));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

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

    public void addRow(String value) {
        rows.add(value);

        JLabel lbl = new JLabel(value);
        lbl.setFont(ROW_FONT);
        lbl.setForeground(LexemizerFrame.FG_WHITE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(3, 4, 3, 4));

        rowContainer.add(lbl);
        rowContainer.add(Box.createVerticalStrut(2));
        rowContainer.revalidate();
        rowContainer.repaint();
    }

    //pang trial lang toh
    public void clearRows() {
        rows.clear();
        rowContainer.removeAll();
        rowContainer.revalidate();
        rowContainer.repaint();
    }

    //pang trial lang toh
    public List<String> getRows() {
        return new ArrayList<>(rows);
    }
}