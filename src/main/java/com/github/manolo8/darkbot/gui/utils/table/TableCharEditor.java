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

public class TableCharEditor extends TableDelegateEditor<JCharField> {

    public TableCharEditor() {
        super(new JCharField());
    }

    @Override
    protected void setValue(Object value) {
        delegate.setValue((Character) value);
        SwingUtilities.invokeLater(delegate::requestFocus);
    }

    @Override
    protected Object getValue() {
        return delegate.getValue();
    }

}
