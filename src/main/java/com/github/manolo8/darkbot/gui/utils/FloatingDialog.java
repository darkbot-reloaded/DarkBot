package com.github.manolo8.darkbot.gui.utils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import java.awt.Insets;

public class FloatingDialog {

    public static void show(JPanel panel, JComponent parent) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.add(panel);
        popup.show(parent, 0, parent.getHeight());
    }

    public static void showAutoHide(JComponent parent, JComponent content, int x, int y, int ms) {
        JPopupMenu popup = new JPopupMenu() {
            @Override
            public Insets getInsets() {
                return new Insets(5, 5, 5, 5);
            }
        };
        popup.add(content);
        popup.show(parent, x, y);

        new Timer(ms, l -> {
            popup.setVisible(false);
            ((Timer) l.getSource()).stop();
        }).start();
    }
}
