package com.github.manolo8.darkbot.gui.tree.components;

import java.awt.*;

public class JLabel extends javax.swing.JLabel {

    public JLabel() {
        this("");
    }

    public JLabel(String text) {
        super(text);

        Font f = getFont();
        setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
    }
}
