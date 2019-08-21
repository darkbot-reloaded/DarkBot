package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;

public interface InstructionProvider {

    String instructions();

    default void showInstructions() {
        Popups.showMessageAsync("Instructions", instructions(), JOptionPane.INFORMATION_MESSAGE);
    }

}
