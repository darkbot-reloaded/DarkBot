package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.MainGui;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class Popups {

    public static void showMessageAsync(String title, Object content, int type) {
        showMessageAsync(title, new JOptionPane(content, type));
    }
    public static void showMessageAsync(String title, JOptionPane pane) {
        SwingUtilities.invokeLater(() -> showMessage(title, pane));
    }

    public static void showMessageSync(String title, JOptionPane pane) {
        try {
            if (SwingUtilities.isEventDispatchThread()) showMessage(title, pane);
            else SwingUtilities.invokeAndWait(() -> showMessage(title, pane));
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void showMessage(String title, JOptionPane pane) {
        JDialog dialog = pane.createDialog(title);
        dialog.setIconImage(MainGui.ICON);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

}
