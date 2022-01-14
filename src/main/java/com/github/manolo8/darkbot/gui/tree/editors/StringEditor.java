package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import java.awt.*;

public class StringEditor extends JTextField implements OptionEditor<String> {

    @Override
    public JComponent getEditorComponent(ConfigSetting<String> string) {
        Integer columns = string.getMetadata("length");
        if (columns == null)
            throw new UnsupportedOperationException("Length must be present in string handler");
        setColumns(columns);

        putClientProperty("JTextField.placeholderText", string.getMetadata("placeholder"));

        setText(string.getValue());
        setEditable(!Boolean.TRUE.equals(string.getMetadata("readonly")));

        return this;
    }

    @Override
    public String getEditorValue() {
        return getText();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
