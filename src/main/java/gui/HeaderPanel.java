package main.java.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class HeaderPanel extends JPanel {

    public HeaderPanel() {
       init();
    }
    private void init(){
         setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 18));
        setBorder(new EmptyBorder(12, 0, 4, 0));

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("resources/compy.io.png"));
        //para sa sizing
        int newWidth = 260;
        int newHeight = 90; 
        Image scaledImage = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logoLabel = new JLabel(scaledIcon);
        add(logoLabel);
    }
}

