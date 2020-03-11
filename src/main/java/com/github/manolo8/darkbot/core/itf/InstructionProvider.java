package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;

public interface InstructionProvider {

    /**
     * Basic instructions, just a string. Will display when the module is set or by user request.
     * @return The string to show as instructions
     */
    default String instructions() {
        return null;
    }

    /**
     * Advanced instructions, a full component to be shown in the option pane. Allows adding text, buttons, etc.
     * @return The JComponent to show as instructions.
     */
    default JComponent instructionsComponent() {
        return null;
    }

    /**
     * Called when the user presses to display instructions or the module is loaded.
     * You usually would customize via {@link #instructions} or {@link #instructionsComponent} instead.
     * @param featureName The name of the feature to display to show instructions for
     */
    default void showInstructions(String featureName) {
        Object display = instructionsComponent();
        if (display == null) display = instructions();
        if (display == null) return; // If both text & component are null, do nothing
        Popups.showMessageAsync(I18n.get("module.instructions.title") + " - " + featureName,
                display, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * @return The instructions to show before config tree in config pop-up
     */
    default JComponent beforeConfig() {
        return null;
    }

}
