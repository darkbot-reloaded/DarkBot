package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainToggleButton extends JToggleButton implements ActionListener {

    public MainToggleButton(String text) {
        this(null, text);
    }

    public MainToggleButton(Icon icon) {
        this(icon, null);
    }

    protected MainToggleButton(Icon icon, String text) {
        this(icon, text, null);
    }

    protected MainToggleButton(Icon icon, String text, String description) {
        super(text, icon);
        putClientProperty("JButton.buttonType", "square");

        addActionListener(this);
        if (description != null && !description.isEmpty()) setToolTipText(description);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    @Override
    public Insets getInsets() {
        return UIUtils.getInsetConfig(getText() != null && !getText().isEmpty());
    }

}
