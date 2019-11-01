package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;

public class TableDoubleEditor extends DefaultCellEditor {

    private NumberFormat nf = NumberFormat.getInstance();
    private Object value;

    public TableDoubleEditor() {
        super(new JTextField());
        JTextField tf = (JTextField) getComponent();
        tf.setName("Table.editor");
        tf.setHorizontalAlignment(JTextField.RIGHT);

        tf.removeActionListener(delegate);
        tf.addActionListener(delegate = new EditorDelegate() {
            public void setValue(Object value) {
                tf.setText(value == null ? "" : nf.format(value));
                tf.selectAll();
            }

            public Object getCellEditorValue() {
                return tf.getText();
            }
        });
    }

    public boolean stopCellEditing() {
        try {
            value = nf.parse((String) super.getCellEditorValue()).doubleValue();
        } catch (Exception e) {
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.red));
            return false;
        }
        return super.stopCellEditing();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        this.value = null;
        ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public Object getCellEditorValue() {
        return value;
    }

}
