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

public class JCharField extends JTextField implements OptionEditor {

    private static final char EMPTY = (char) 0;
    private static final DocumentFilter SINGLE_CHAR_DOCUMENT = new DocumentFilter(){
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, 0, fb.getDocument().getLength(), text == null || text.isEmpty() || text.equals("\b") ? "" : text.substring(text.length() - 1), attrs);
        }
    };

    private ConfigField field;

    public JCharField() {
        putClientProperty("ConfigTree", true);
        ((AbstractDocument) getDocument()).setDocumentFilter(SINGLE_CHAR_DOCUMENT);
        this.getDocument().addDocumentListener((GeneralDocumentListener) e ->  {
            if (field != null) field.set(getValue());
        });
        setPreferredSize(new Dimension(20, 16));
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

    public Character getValue() {
        if (field != null && field.isPrimitive() && getText().isEmpty()) return EMPTY;
        return getText().isEmpty() ? null : getText().charAt(0);
    }

}
