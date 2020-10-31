package com.github.manolo8.darkbot.gui.utils.table;

import javax.swing.*;

public class TableDoubleRenderer extends TableDelegateRenderer<JSpinner> {

    public TableDoubleRenderer() {
        super(new JSpinner());
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue(value);
    }
}
