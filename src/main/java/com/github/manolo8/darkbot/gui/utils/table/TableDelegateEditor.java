package com.github.manolo8.darkbot.gui.utils.table;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public abstract class TableDelegateEditor<T extends JComponent> extends AbstractCellEditor implements TableCellEditor {

    protected final T delegate;

    public TableDelegateEditor(T delegate) {
        this.delegate = delegate;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        delegate.setBackground(table.getBackground());
        delegate.setForeground(table.getForeground());

        delegate.setBorder(UIManager.getBorder(
                isSelected ? "Table.focusSelectedCellHighlightBorder" : "Table.focusCellHighlightBorder"));

        setValue(value);
        startEditing(table, value, isSelected, row, column);
        return delegate;
    }

    protected void startEditing(JTable table, Object value, boolean isSelected, int row, int column) {}

    protected abstract void setValue(Object value);
    protected abstract Object getValue();

    public Object getCellEditorValue() {
        return getValue();
    }
}