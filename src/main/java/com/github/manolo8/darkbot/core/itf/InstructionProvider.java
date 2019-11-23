package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;

public interface InstructionProvider {

    String instructions();

    default void showInstructions() {
        Popups.showMessageAsync(I18n.get("module.instructions.title"), instructions(), JOptionPane.INFORMATION_MESSAGE);
    }

}
