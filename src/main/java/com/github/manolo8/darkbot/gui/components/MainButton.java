package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainButton extends JButton implements ActionListener {

    protected Color actionColor; // Currently useless.
    protected boolean defaultInsets;

    public MainButton(String text) {
        this(null, text);
    }

    public MainButton(Icon icon) {
        this(icon, null);
    }

    public MainButton(Icon icon, boolean defaultInsets) {
        this(icon);
        this.defaultInsets = defaultInsets;
    }

    protected MainButton(Icon icon, String text) {
        this(icon, text, null);
    }

    protected MainButton(Icon icon, String text, String description) {
        super(text, icon);

        addActionListener(this);
        if (description != null && !description.isEmpty()) setToolTipText(description);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    @Override
    public Insets getInsets() {
        if (defaultInsets) return super.getInsets();
        return UIUtils.getInsetConfig(getText() != null && !getText().isEmpty() && getText().length() > 1);
    }

    @Override
    public boolean isDefaultButton() {
        return false; // Avoid painting as blue & focused
    }
}
