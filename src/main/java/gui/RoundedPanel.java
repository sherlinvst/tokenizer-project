package main.java.gui;

import javax.swing.*;
import java.awt.*;

//pang round lang toh ng corner ng bawat panel
public class RoundedPanel extends JPanel {

    private final int radius;

    public RoundedPanel(int radius) {
        this.radius = radius;
        setOpaque(false);
        setBackground(LexemizerFrame.PANEL_BG);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
