package main.java.gui.popups;

import java.awt.*;
import javax.swing.*;

public class TutPop extends Popup {

    private static final String[] MESSAGES = {
        "Alright, let's get you familiar with the interface!",
        "This is the Input Panel. Type or paste your source code here.",
        "Above the input panel are buttons to clear and to compile your code.",
        "Lastly, the right-most panel is the Output Panel. It displays the output of your code.",
        "You're all set! Click OKAY to start using compy.io."
    };

    private static final String[] ICONS = {
        "src/main/java/gui/resources/TutPop.png",
        "src/main/java/gui/resources/TutCod.png",
        "src/main/java/gui/resources/TutBtn.png",
        "src/main/java/gui/resources/TutTok.png",
        "src/main/java/gui/resources/TutPop.png"
    };

    private static final Position[] POSITIONS = {
        Position.CENTER,
        Position.CENTER_LEFT,
        Position.CENTER_LEFT,
        Position.CENTER_RIGHT,
        Position.CENTER
    };

    private final int step;

    public TutPop(Frame parent, int step) {
        super(parent, "TUTORIAL (" + step + "/5)", MESSAGES[step - 1], new ImageIcon(
            new ImageIcon(ICONS[step - 1])
            .getImage()
            .getScaledInstance(75, 75, Image.SCALE_SMOOTH)
        ));
        this.step = step;
        initComponents();
    }

    private void initComponents() {
        if (step < 5) {
            PopBtn nextBtn = new PopBtn("NEXT", true);
            nextBtn.addActionListener(e -> {
                dispose();
                new TutPop((Frame) getParent(), step + 1).showPopup(POSITIONS[step]);
            });
            addButton(nextBtn);
        } else {
            PopBtn okBtn = new PopBtn("OKAY", true);
            okBtn.addActionListener(e -> dispose());
            addButton(okBtn);
        }
    }
}