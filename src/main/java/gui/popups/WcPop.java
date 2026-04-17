package main.java.gui.popups;
import java.awt.*;
import javax.swing.*;

public class WcPop extends Popup {

    private static final String MSG = "This is compy.io! A compiler for Java. Would you like to see a quick tutorial on how to use the application?";
    
    public WcPop(Frame parent) {
        super(parent, "WELCOME!", MSG, new ImageIcon(
            new ImageIcon("src/main/java/gui/resources/compy.io.png")
            .getImage()
            .getScaledInstance(150, 75, Image.SCALE_SMOOTH)
        ));
        initComponents();
    }

    private void initComponents() {
        PopBtn yBtn = new PopBtn("YES", true);
        PopBtn nBtn = new PopBtn("NO", true);
        yBtn.addActionListener(e -> {
            dispose();
            new TutPop((Frame) getParent(), 1).showPopup(Position.CENTER);
        });
        nBtn.addActionListener(e -> dispose());
        addButton(yBtn);
        addButton(nBtn);
    }
}