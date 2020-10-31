package com.github.manolo8.darkbot.gui.utils.table;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class TableDoubleEditor extends AbstractCellEditor implements TableCellEditor {

    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, null, null, 10));

    public TableDoubleEditor() {
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinner.setValue(value);
        spinner.setBorder(DefaultLookup.getBorder(spinner, table.getUI(), "Table.cellNoFocusBorder"));
        return spinner;
    }

    public Object getCellEditorValue() {
        return ((Number) spinner.getValue()).doubleValue();
    }

}
