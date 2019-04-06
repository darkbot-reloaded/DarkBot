package com.github.manolo8.darkbot.gui.components;

import com.bulenkov.iconloader.util.Gray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainButton extends JButton implements MouseListener {

    public MainButton(String text) {
        super(text);
        putClientProperty("JButton.buttonType", "square");
        setBorder(BorderFactory.createLineBorder(Gray._100));

        addMouseListener(this);
    }

    @Override
    public Insets getInsets() {
        return new Insets(3, 3, 3, 3);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
