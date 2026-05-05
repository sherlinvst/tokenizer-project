package main.java;

import javax.swing.SwingUtilities;
import main.java.gui.LexemizerFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
          LexemizerFrame frame = new LexemizerFrame();
          frame.start();
      });
    }
}