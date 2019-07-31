package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.MainGui;

import javax.swing.*;

public class Popups {

    public static void showMessageAsync(String title, String content, int type) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane pane = new JOptionPane(content, type);
            JDialog dialog = pane.createDialog(title);
            dialog.setIconImage(MainGui.ICON);
            dialog.setAlwaysOnTop(true);
            dialog.setVisible(true);
        });
    }

}
