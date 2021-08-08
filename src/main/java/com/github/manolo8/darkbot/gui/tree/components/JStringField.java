package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Length;
import com.github.manolo8.darkbot.config.types.Placeholder;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import eu.darkbot.api.config.annotations.Text;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class JStringField extends JTextField implements OptionEditor {

    private ConfigField field;

    public JStringField() {
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
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
        setText(Objects.toString(field.get(), ""));
        Text text = field.field.getAnnotation(Text.class);

        Length len = field.field.getAnnotation(Length.class);
        setColumns(len != null ? len.value() : text != null ? text.length() : 10);

        Placeholder ph = field.field.getAnnotation(Placeholder.class);
        putClientProperty("JTextField.placeholderText", ph != null ? ph.value() :
                text != null && !text.placeholder().isEmpty() ? text.placeholder() : null);

        this.field = field;
    }

    public String getValue() {
        return getText().isEmpty() ? null : getText();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
