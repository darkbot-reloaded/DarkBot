package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.awt.*;

public class JBoolField extends JCheckBox implements OptionEditor {

    private ConfigField field;

    public JBoolField() {
        super.addChangeListener(e -> {
            if (field != null) field.set(this.isSelected());
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setSelected(field.get());
        this.field = field;
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, -1, 0, 0);
    }
}
