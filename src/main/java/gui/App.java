package main.java.gui;

import javax.swing.SwingUtilities; 
//pang run lang toh ng ui
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LexemizerFrame frame = new LexemizerFrame();
            frame.setVisible(true);
        });
    }
}