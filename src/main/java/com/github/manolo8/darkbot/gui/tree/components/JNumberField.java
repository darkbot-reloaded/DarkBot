package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import eu.darkbot.api.config.annotations.Number;

import javax.swing.*;
import java.awt.*;

public class JNumberField extends JSpinner implements OptionEditor {

    private ConfigField field;

    public JNumberField() {
        addChangeListener(e -> {
            if (field != null) field.set(getValue());
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        Num num = field.field.getAnnotation(Num.class);
        Number number = field.field.getAnnotation(Number.class);

        java.lang.Number value = field.get();

        java.lang.Number oldVal = value;
        if (number != null) {
            if (value.doubleValue() < number.min()) value = number.min();
            if (value.doubleValue() > number.max()) value = number.max();
        } else {
            if (value.intValue() < num.min()) value = num.min();
            if (value.intValue() > num.max()) value = num.max();
        }
        if (!oldVal.equals(value))
            System.err.println("Invalid config value was detected, and bound to min-max restriction: " + field.field);

        SpinnerNumberModel model;
        try {
            model = new SpinnerNumberMinMaxFix(value, num.min(), num.max(), num.step());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.err.println("Failed to create editor for field, ignoring min & max: " + field.field);

            model = new SpinnerNumberMinMaxFix(value, null, null, num.step());
        }
        setModel(model);
        setPreferredSize(new Dimension(25 + (String.valueOf(num.max()).length() * 9), AdvancedConfig.EDITOR_HEIGHT));
        this.field = field;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
