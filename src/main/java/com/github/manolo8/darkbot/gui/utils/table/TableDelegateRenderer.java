package com.github.manolo8.darkbot.gui.utils.table;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public abstract class TableDelegateRenderer<T extends JComponent> implements TableCellRenderer {

    protected final T delegate;

    public TableDelegateRenderer(T delegate) {
        this.delegate = delegate;
        this.delegate.setBorder(BorderFactory.createEmptyBorder());
    }

    protected abstract void setValue(Object value);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        setValue(value);
        delegate.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        delegate.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());

        delegate.setBorder(DefaultLookup.getBorder(delegate, table.getUI(),
                hasFocus && isSelected ? "Table.focusSelectedCellHighlightBorder" : "Table.cellNoFocusBorder"));

        return delegate;
    }

}
