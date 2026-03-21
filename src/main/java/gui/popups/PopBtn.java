package main.java.gui.popups;
import java.awt.*; 
import javax.swing.*;

public class PopBtn extends JButton {
    private boolean filled;
    private final Color baseColor = new Color(14, 0, 34);

    public PopBtn(String text, boolean filled) {
        super(text);        
        this.filled = filled;
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        setFont(new Font("Arial", Font.BOLD, 13));
        setForeground(filled ? Color.WHITE : baseColor);
        setBackground(filled ? baseColor : new Color(14, 0, 34, 50)); 
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void layoutComponents() {
        setPreferredSize(new Dimension(100, 38));
    }

    @Override
    protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    if (getModel().isPressed()) {
        g2.setColor(getBackground().brighter()); 
    } else {
        g2.setColor(filled ? getBackground() : new Color(255, 255, 255, 50)); 
    }

    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
    g2.dispose();
    super.paintComponent(g);
    }
}