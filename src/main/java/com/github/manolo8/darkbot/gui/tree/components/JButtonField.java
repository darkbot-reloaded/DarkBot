package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;

public class JButtonField extends JButton implements OptionEditor {
    private ConfigField field;

    public JButtonField() {

    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = field;
    }
}
