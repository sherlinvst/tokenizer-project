package test;
import main.java.gui.LexemizerFrame;
import javax.swing.SwingUtilities;

public class GuiTest {
  public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
          LexemizerFrame frame = new LexemizerFrame();
          frame.start();
      });
  }
}


