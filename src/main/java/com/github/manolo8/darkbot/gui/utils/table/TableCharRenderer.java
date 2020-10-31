package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.gui.tree.components.JCharField;

public class TableCharRenderer extends TableDelegateRenderer<JCharField> {

    public TableCharRenderer() {
        super(new JCharField());
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue((Character) value);
    }

}
