package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.util.Objects;

public class JButtonField extends JButton implements OptionEditor {
    private JButton button;
    private ConfigField field;

    public JButtonField() {
        if (button == null) {
            button = new JButton();
        }

    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = field;
        setText(Objects.toString(field.get(), ""));
    }
}
