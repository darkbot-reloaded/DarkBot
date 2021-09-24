package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.MainGui;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class Popups {

    public static void showMessageAsync(String title, String content, int type) {
        showMessageAsync(title, (Object) content, type);
    }

    public static void showMessageAsync(String title, Object content, int type) {
        showMessageAsync(title, new JOptionPane(content, type));
    }

    public static void showMessageAsync(String title, JOptionPane pane) {
        SwingUtilities.invokeLater(() -> showMessage(null, title, pane, null));
    }

    public static void showMessageSync(String title, JOptionPane pane) {
        showMessageSync(title, pane, null);
    }

    public static void showMessageSync(String title, JOptionPane pane, Consumer<JDialog> callback) {
        showMessageSync(null, title, pane, callback);
    }

    public static void showMessageSync(Component parent, String title, JOptionPane pane, Consumer<JDialog> callback) {
        try {
            if (SwingUtilities.isEventDispatchThread()) showMessage(parent, title, pane, callback);
            else SwingUtilities.invokeAndWait(() -> showMessage(parent, title, pane, callback));
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void showMessage(Component parent, String title, JOptionPane pane, Consumer<JDialog> callback) {
        JDialog dialog = parent == null ? pane.createDialog(title) : pane.createDialog(parent, title);
        if (callback != null) callback.accept(dialog);
        dialog.setIconImage(MainGui.ICON);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

}
