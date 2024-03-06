package com.github.manolo8.darkbot.gui.utils.table;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

public class TableDoubleEditor extends TableDelegateEditor<JSpinner> {

    public TableDoubleEditor() {
        super(new JSpinner(new SpinnerNumberModel(0, null, null, 10)) {
            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        });
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue(value);
    }

    @Override
    protected Object getValue() {
        return ((Number) delegate.getValue()).doubleValue();
    }

    @Override
    public boolean stopCellEditing() {
        try {
            delegate.commitEdit();
        } catch (ParseException ignored) {}
        return super.stopCellEditing();
    }
}
