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

    private JLabel  headerLabel;
    private JPanel  rowContainer;
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
        headerLabel  = buildHeader();
        rowContainer = new JPanel();
        scrollPane   = buildScrollPane();
    }

    private JLabel buildHeader() {
        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(HEADER_FONT);
        lbl.setForeground(LexemizerFrame.FG_WHITE);
        lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
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

        styleScrollBar(sp);

        return sp;
    }

    private void layoutComponents() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(headerLabel, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2A2550));
        top.add(sep, BorderLayout.SOUTH);

        add(top,        BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    protected void styleScrollBar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(80, 70, 140);
                
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
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

    public void clearRows() {
        rows.clear();
        rowContainer.removeAll();
        rowContainer.revalidate();
        rowContainer.repaint();
    }

    public List<String> getRows() {
        return new ArrayList<>(rows);
    }
}