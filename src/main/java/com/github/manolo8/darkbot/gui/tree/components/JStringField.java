package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.Objects;

public class JStringField extends JTextField implements OptionEditor {

    private ConfigField field;

    public JStringField() {
        putClientProperty("ConfigTree", true);
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
            if (field != null) field.set(getValue());
        });
        setPreferredSize(new Dimension(30, 16));
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        setText(Objects.toString(field.get(), ""));
        this.field = field;
    }

    public String getValue() {
        return getText().isEmpty() ? null : getText();
    }

}
