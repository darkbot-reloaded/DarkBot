package com.github.manolo8.darkbot.gui.tree.components;

import com.bulenkov.iconloader.util.Gray;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class JListField<T> extends JComboBox<T> implements OptionEditor {

    private OptionList<Object> options;
    private ConfigField field;

    public JListField() {
        putClientProperty("ConfigTree", true);
        putClientProperty("JComboBox.isTableCellEditor", true);
        setBorder(BorderFactory.createLineBorder(Gray._90));
        addActionListener(e -> {
            if (field != null) field.set(options.getValue((String) getSelectedItem()));
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
            }
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

        if (getModel() != options) setModel((ComboBoxModel<T>) options);

        Object option = options.getText(field.get());
        if (getSelectedItem() != option) setSelectedItem(option);

        this.field = field;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
