package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class JBoolField extends JCheckBox implements OptionEditor {

    private ConfigField field;
    private long valueSet;

    public JBoolField() {
        putClientProperty("ConfigTree", true);
        super.addChangeListener(e -> {
            if (field != null) field.set(this.isSelected());
        });
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (System.currentTimeMillis() - valueSet > 1) super.processMouseEvent(e);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setSelected(field.get());
        valueSet = System.currentTimeMillis();
        this.field = field;
    }

    public Boolean getValue() {
        return this.isSelected();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
