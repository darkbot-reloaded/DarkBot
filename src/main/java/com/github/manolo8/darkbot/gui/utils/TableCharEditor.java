package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.tree.components.JCharField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class TableCharEditor extends AbstractCellEditor implements TableCellEditor {

    private final TableCharFieldEditor field = new TableCharFieldEditor();
    private Character currValue;

    public TableCharEditor() {
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        table.requestFocus();
        this.field.setValue((Character) value);
        return field;
    }

    public Object getCellEditorValue() {
        return currValue;
    }

    private class TableCharFieldEditor extends JCharField {
        @Override
        protected void setValue(Character value) {
            super.setValue(value);
            currValue = value;
        }
    }

}
