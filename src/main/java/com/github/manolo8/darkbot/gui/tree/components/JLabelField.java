package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.util.Objects;

public class JLabelField extends JLabel implements OptionEditor {

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        setText(Objects.toString(field.get(), ""));
    }

}
