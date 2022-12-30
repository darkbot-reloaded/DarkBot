package com.github.manolo8.darkbot.gui.components;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainToggleButton extends JToggleButton implements ActionListener {

    public MainToggleButton(Icon icon) {
        this(icon, null, null);
    }

    protected MainToggleButton(Icon icon, String text, String description) {
        super(text, icon);

        addActionListener(this);
        if (description != null && !description.isEmpty()) setToolTipText(description);
    }

    @Override
    public void actionPerformed(ActionEvent e) {}

}
