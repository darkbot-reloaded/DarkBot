package com.github.manolo8.darkbot.gui.tree.components;

import java.awt.event.KeyEvent;

@Deprecated
public class JCharField {
    public static String getDisplay(Character ch) {
        if (ch == null || ch == '\0') return "";
        return KeyEvent.getKeyText(ch);
    }
}
