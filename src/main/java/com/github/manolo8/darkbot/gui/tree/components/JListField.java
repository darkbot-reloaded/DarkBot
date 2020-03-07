package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.ToolTipListRenderer;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;

public class JListField extends JComboBox<String> implements OptionEditor {

    private Map<Class<? extends OptionList<?>>, OptionList<?>> optionInstances = new HashMap<>();
    private OptionList<?> options;
    protected ConfigField field;

    public JListField() {
        putClientProperty("JComboBox.isTableCellEditor", true);
        addActionListener(event -> {
            if (field != null) updateValue();
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
            }
        });
        setRenderer(new ToolTipListRenderer(this));
    }

    protected void updateValue() {
        field.set(options.getValue((String) getSelectedItem()));
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        options = optionInstances.computeIfAbsent(
                field.field.getAnnotation(Options.class).value(), ReflectionUtils::createInstance);

        if (getModel() != options) setModel(options);

        Object option = options.getText(field.get());
        if (getSelectedItem() != option) setSelectedItem(option);

        this.field = field;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    public String getToolTipFor(String value) {
        if (options == null) return null;
        return options.getTooltip(value);
    }

}
