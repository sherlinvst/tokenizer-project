package main.java.gui.popups;

import java.awt.*;
import javax.swing.*;
import main.java.gui.RoundedPanel;

public class Popup extends JDialog {
    protected RoundedPanel container;
    protected GridBagConstraints gbc;
    public boolean confirmed = false;
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    public enum Position {
        CENTER, TOP_LEFT, TOP_RIGHT, CENTER_LEFT, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private final String title;
    private final String message;
    private final Icon icon;

    public Popup(Frame parent, String title, String message, Icon icon) {
        super(parent, true);
        this.title = title;
        this.message = message;
        this.icon = icon;
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        
        container = new RoundedPanel(16);
        container.setBackground(Color.WHITE);
        container.setLayout(new GridBagLayout());
    }

    private void layoutComponents() {
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 30, 15, 30);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            gbc.gridy = 0;
            container.add(iconLabel, gbc);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridy = 1;
        container.add(titleLabel, gbc);

        JLabel msgLabel = new JLabel("<html><body style='width: 250px; text-align: center;'>" + message + "</body></html>");
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        msgLabel.setForeground(new Color(14, 0, 34));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 30, 20, 30);
        container.add(msgLabel, gbc);

        buttonPanel.setOpaque(false);
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 5, 20, 5);
        container.add(buttonPanel, gbc);

        setContentPane(container);
    }

    protected void addButton(JButton button) {
        buttonPanel.add(button);
    }

    public void showPopup() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    public void showPopup(Position position) {
    pack();
    int frameX = getOwner().getX();
    int frameY = getOwner().getY();
    int frameW = getOwner().getWidth();
    int frameH = getOwner().getHeight();

    switch (position) {
        case CENTER       -> setLocationRelativeTo(getOwner());
        case CENTER_LEFT  -> setLocation(frameX + 20, frameY + (frameH - getHeight()) / 2);
        case CENTER_RIGHT -> setLocation(frameX + frameW - getWidth() - 20, frameY + (frameH - getHeight()) / 2);
        case TOP_LEFT     -> setLocation(frameX + 20, frameY + 20);
        case TOP_RIGHT    -> setLocation(frameX + frameW - getWidth() - 20, frameY + 20);
        case BOTTOM_LEFT  -> setLocation(frameX + 20, frameY + frameH - getHeight() - 20);
        case BOTTOM_RIGHT -> setLocation(frameX + frameW - getWidth() - 20, frameY + frameH - getHeight() - 20);
    }
    setVisible(true);
    }
}