package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;

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
        this.removeAllItems();
        this.options = ReflectionUtils.createInstance(field.field.getAnnotation(Options.class).value()).get();
        for (String option : options.getOptions()) addItem(option);

        this.setPreferredSize(null);
        setSelectedItem(options.getText(field.get()));
        this.field = field;
    }
}
