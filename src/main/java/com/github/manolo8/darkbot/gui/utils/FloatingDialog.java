package com.github.manolo8.darkbot.gui.utils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class FloatingDialog {

    public static void show(JPanel panel, JComponent parent) {
        show(panel, parent, 0, parent.getHeight());
    }

    public static JPopupMenu show(JComponent component, JComponent parent, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.add(component);
        popup.show(parent, x, y);
        return popup;
    }
}
