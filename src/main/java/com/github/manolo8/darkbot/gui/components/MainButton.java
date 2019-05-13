package com.github.manolo8.darkbot.gui.components;

import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class MainButton extends JButton implements SimpleMouseListener, ActionListener {

    protected Color actionColor = new Color(50, 53, 55);
    private Color def;

    private boolean hovering, pressing;

    public MainButton(String text) {
        this(null, text);
    }

    public MainButton(Icon icon) {
        this(icon, null);
    }

    private MainButton(Icon icon, String text) {
        super(text, icon);
        putClientProperty("JButton.buttonType", "square");
        setBorder(UIUtils.getBorder());

        this.def = getBackground();
        addMouseListener(this);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    @Override
    public Insets getInsets() {
        return UIUtils.getInsetConfig(getText() != null && !getText().isEmpty());
    }

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

    protected void setBackground() {
        if (pressing) setBackground(actionColor.darker());
        else if (hovering) setBackground(actionColor);
        else setBackground(def);
    }

}
