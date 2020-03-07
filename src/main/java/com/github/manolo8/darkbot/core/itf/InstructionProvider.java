package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;

public interface InstructionProvider {

    String instructions();

    default void showInstructions() {
        Object display = instructionsDisplay();
        if (display == null) display = instructions();
        Popups.showMessageAsync(I18n.get("module.instructions.title"), display, JOptionPane.INFORMATION_MESSAGE);
    }

    default JComponent instructionsDisplay() {
        return null;
    }

}
