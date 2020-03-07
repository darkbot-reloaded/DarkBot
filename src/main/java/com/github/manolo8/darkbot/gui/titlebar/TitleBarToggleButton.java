package com.github.manolo8.darkbot.gui.titlebar;

import com.github.manolo8.darkbot.gui.components.MainToggleButton;

import javax.swing.*;
import java.awt.event.ActionListener;

abstract class TitleBarToggleButton<T extends JFrame> extends MainToggleButton implements ActionListener {

    T frame;

    TitleBarToggleButton(Icon icon, T frame) {
        super(icon);
        this.frame = frame;
    }

    TitleBarToggleButton(String text, T frame) {
        super(text);
        this.frame = frame;
    }

}
