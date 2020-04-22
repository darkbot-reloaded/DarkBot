package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.MainGui;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class Popups {

    public static void showMessageAsync(String title, Object content, int type) {
        showMessageAsync(title, new JOptionPane(content, type));
    }

    public static void showMessageAsync(String title, JOptionPane pane) {
        SwingUtilities.invokeLater(() -> showMessage(title, pane, null));
    }

    public static void showMessageSync(String title, JOptionPane pane) {
        showMessageSync(title, pane, null);
    }

    public static void showMessageSync(String title, JOptionPane pane, Consumer<JDialog> callback) {
        try {
            if (SwingUtilities.isEventDispatchThread()) showMessage(title, pane, callback);
            else SwingUtilities.invokeAndWait(() -> showMessage(title, pane, callback));
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void showMessage(String title, JOptionPane pane, Consumer<JDialog> callback) {
        JDialog dialog = pane.createDialog(title);
        if (callback != null) callback.accept(dialog);
        dialog.setIconImage(MainGui.ICON);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

}
