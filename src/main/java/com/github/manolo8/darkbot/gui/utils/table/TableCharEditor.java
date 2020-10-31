package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.gui.tree.components.JCharField;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TableCharEditor extends AbstractCellEditor implements TableCellEditor, CellEditorListener, FocusListener {

    private final JCharField delegate = new JCharField();
    private boolean isEditing;

    public TableCharEditor() {
        delegate.setBorder(BorderFactory.createEmptyBorder());
        delegate.addFocusListener(this);
        addCellEditorListener(this);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.delegate.setBackground(table.getBackground());
        this.delegate.setForeground(table.getForeground());

        this.delegate.setValue((Character) value);
        SwingUtilities.invokeLater(() -> {
            isEditing = true;
            delegate.requestFocus();
        });
        return delegate;
    }

    public Object getCellEditorValue() {
        return delegate.getValue();
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        isEditing = false;
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        isEditing = false;
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        // Stop editing if focus is lost and it was editing
        if (isEditing) stopCellEditing();
    }

}
