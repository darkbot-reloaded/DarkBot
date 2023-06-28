package com.github.manolo8.darkbot.gui.utils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

public class FloatingDialog {

    public static void show(JPanel panel, JComponent parent) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.add(panel);
        popup.show(parent, 0, parent.getHeight());
    }
}
