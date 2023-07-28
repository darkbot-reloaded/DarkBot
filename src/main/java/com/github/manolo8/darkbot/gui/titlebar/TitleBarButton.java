package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract class TitleBarButton<T extends JFrame> extends JButton implements ActionListener {

    protected T frame;

    TitleBarButton(Icon icon, T frame) {
        super(icon);
        this.frame = frame;

        configureButton(this);

        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

    public static void configureButton(AbstractButton button) {
        button.setMinimumSize(new Dimension(24, 30));
        button.setPreferredSize(new Dimension(30, 30));
        button.setMaximumSize(new Dimension(44, 30));
        button.setFocusable(false);
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
    }

}
