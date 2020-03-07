package com.github.manolo8.darkbot.gui.safety;

import com.github.manolo8.darkbot.config.SafetyInfo;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

class SafetiesList extends JTable {

    private SafetiesEditor editor;
    private SafetyTableModel model = new SafetyTableModel();

    SafetiesList(SafetiesEditor editor) {
        this.editor = editor;
        setModel(model);
        setTableHeader(null);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);
        setFocusable(false);
        setRowHeight(20);
        setDefaultRenderer(SafetyInfo.class, new SafetyCellRenderer());

        getSelectionModel().addListSelectionListener(lse -> {
            if (!lse.getValueIsAdjusting())
                editor.edit(getSelectedRow() == -1 ? null : (SafetyInfo) getValueAt(getSelectedRow(), 0));
        });
    }

    void addOrUpdate(SafetyInfo safety) {
        model.addOrUpdate(safety);
    }

    void refresh() {
        model.setRowCount(0);
        for (SafetyInfo safetyInfo : editor.safetyInfos) addOrUpdate(safetyInfo);
    }

    void setSelected(SafetyInfo safety) {
        if (getSelectedRow() != -1 && getValueAt(getSelectedRow(), 0) == safety) return;
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, 0) != safety) continue;
            setRowSelectionInterval(i, i);
            return;
        }
    }

    private class SafetyTableModel extends DefaultTableModel {
        SafetyTableModel() {
            super(0, 1);
        }

        void addOrUpdate(SafetyInfo safety) {
            boolean remove = safety.entity == null || safety.entity.removed;
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0) != safety) continue;
                if (remove) removeRow(i);
                return;
            }
            if (!remove) addRow(new Object[]{safety});
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return SafetyInfo.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class SafetyCellRenderer extends DefaultTableCellRenderer {

        Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
        @Override
        public JComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                        boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createCompoundBorder(getBorder(), padding));
            return this;
        }
    }

}



