package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

public class MainIcon extends JButton {

    public MainIcon(String text) {
        this(null, text);
    }

    public MainIcon(Icon icon) {
        this(icon, null);
    }

    protected MainIcon(Icon icon, String text) {
        super(text, icon);
        putClientProperty("JButton.buttonType", "square");
        setBorder(UIUtils.getBorder());
    }

    @Override
    public Insets getInsets() {
        return UIUtils.getInsetConfig(getText() != null && !getText().isEmpty());
    }

}
