package main.java.gui.popups;

import java.awt.*;
import javax.swing.*;

public class WarnPop extends Popup {

    private static final String MSG = "The code area is empty. Please enter some code before proceeding to get token.";
    
    public WarnPop(Frame parent) {
        super(parent, "WARNING!", MSG, new ImageIcon(
            new ImageIcon("src/main/java/gui/resources/WavyWarning.png")
            .getImage()
            .getScaledInstance(75, 75, Image.SCALE_SMOOTH)
        ));
        initComponent();
    }

    private void initComponent() {
        PopBtn okBtn = new PopBtn("OKAY", true);
        okBtn.addActionListener(e -> dispose());
        addButton(okBtn);
    }
}
