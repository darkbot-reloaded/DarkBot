package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.tree.components.JCharField;

import javax.swing.*;

public class TableCharEditor extends DefaultCellEditor {
    public TableCharEditor() {
        super(new JCharField());
        JCharField charField = (JCharField) getComponent();
        charField.removeActionListener(delegate);
        charField.addActionListener(delegate = new EditorDelegate() {
            public void setValue(Object value) {
                charField.setText(value == null ? "" : value.toString());
            }

            public Object getCellEditorValue() {
                return charField.getValue();
            }
        });
    }
}
