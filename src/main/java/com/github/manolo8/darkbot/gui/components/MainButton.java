package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class MainButton extends JButton implements ActionListener {

    protected Color actionColor = UIUtils.ACTION;
    protected Color def;

    private boolean hovering, pressing;

    public MainButton(String text) {
        this(null, text);
    }

    public MainButton(Icon icon) {
        this(icon, null);
    }

    protected MainButton(Icon icon, String text) {
        this(icon, text, null);
    }

    protected MainButton(Icon icon, String text, String description) {
        super(text, icon);
        putClientProperty("JButton.buttonType", "square");

        addActionListener(this);
        setBackground();
        if (description != null && !description.isEmpty()) setToolTipText(description);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setBackground();
    }

    @Override
    public Insets getInsets() {
        return UIUtils.getInsetConfig(getText() != null && !getText().isEmpty());
    }

    protected void setBackground() {
        if (true) return;
        if (isEnabled() && pressing) setBackground(actionColor.darker());
        else if (isEnabled() && hovering) setBackground(actionColor);
        else setBackground(def);
    }

}
