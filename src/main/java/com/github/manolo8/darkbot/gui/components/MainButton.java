package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class MainButton extends MainIcon implements SimpleMouseListener, ActionListener {

    protected Color actionColor = new Color(50, 53, 55);
    protected Color def;

    private boolean hovering, pressing;

    public MainButton(String text) {
        this(null, text);
    }

    public MainButton(Icon icon) {
        this(icon, null);
    }

    protected MainButton(Icon icon, String text) {
        super(icon, text);
        this.def = getBackground();
        addMouseListener(this);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        hovering = true;
        setBackground();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hovering = false;
        setBackground();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressing = true;
        setBackground();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressing = false;
        setBackground();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setBackground();
    }

    protected void setBackground() {
        if (isEnabled() && pressing) setBackground(actionColor.darker());
        else if (isEnabled() && hovering) setBackground(actionColor);
        else setBackground(def);
    }

}
