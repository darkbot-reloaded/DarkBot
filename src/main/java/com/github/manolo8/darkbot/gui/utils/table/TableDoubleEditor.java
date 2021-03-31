package com.github.manolo8.darkbot.gui.utils.table;

import javax.swing.*;

public class TableDoubleEditor extends TableDelegateEditor<JSpinner> {

    public TableDoubleEditor() {
        super(new JSpinner(new SpinnerNumberModel(0, null, null, 10)));
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue(value);
    }

    @Override
    protected Object getValue() {
        return ((Number) delegate.getValue()).doubleValue();
    }
}
