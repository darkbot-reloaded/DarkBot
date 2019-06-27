package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import java.awt.*;

public class JListField extends JComboBox<String> implements OptionEditor {

    private OptionList<Object> options;
    private ConfigField field;

    public JListField() {
        putClientProperty("ConfigTree", true);
        putClientProperty("JComboBox.isTableCellEditor", true);
        addActionListener(e -> {
            if (field != null) field.set(options.getValue((String) getSelectedItem()));
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        //noinspection unchecked
        this.options = ReflectionUtils.createSingleton(field.field.getAnnotation(Options.class).value());
        setModel(options);

        setSelectedItem(options.getText(field.get()));
        this.field = field;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
