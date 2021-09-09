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
        ValueHandler<String> handler = string.getHandler();

        Integer columns = handler.getMetadata("length");
        if (columns == null)
            throw new UnsupportedOperationException("Length must be present in string handler");
        setColumns(columns);

        putClientProperty("JTextField.placeholderText", handler.getMetadata("placeholder"));

        setText(string.getValue());

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
