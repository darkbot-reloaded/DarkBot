package com.github.manolo8.darkbot.gui.players;

import javax.swing.*;

public class TagColorPicker extends JPanel {

    public TagColorPicker() {
        add(new JTextField(20));
        add(new JColorChooser());
    }

}
