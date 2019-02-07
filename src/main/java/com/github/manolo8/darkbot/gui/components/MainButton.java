package com.github.manolo8.darkbot.gui.components;

import com.bulenkov.iconloader.util.Gray;

import javax.swing.*;
import java.awt.*;

public class MainButton extends JButton {

    public MainButton(String text) {
        super(text);
        putClientProperty("JButton.buttonType", "square");
        setBorder(BorderFactory.createLineBorder(Gray._100));
    }

    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }
}
