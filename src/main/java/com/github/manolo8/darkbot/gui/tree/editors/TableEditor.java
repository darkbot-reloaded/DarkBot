package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.components.InfoTable;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TableEditor implements OptionEditor<Map<String, Object>> {

    private final PluginAPI api;

    private ConfigSetting<Map<String, Object>> setting;
    private Map<String, Object> value;
    private Class<?> type;

    private final JScrollPane scrollPane;
    private final JTable tableComponent;
    private final ToolTipHeader header;
    private ListSelectionModel selectionModel;
    private GenericTableModel<Object> tableModel;
    private BoundedRangeModel scrollModel;

    private Consumer<Map<String, Object>> update = v -> tableModel.setConfig(v);

    public TableEditor(PluginAPI api) {
        this.api = api;

        this.tableComponent = new JTable();
        this.tableComponent.setTableHeader(header = new ToolTipHeader(tableComponent.getColumnModel()));

        this.scrollPane = new JScrollPane(tableComponent);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Map<String, Object>> table) {
        this.setting = table;
        this.value = table.getValue();
        ValueHandler<Map<String, Object>> handler = table.getHandler();

        this.type = handler.getMetadata("table.type");
        this.selectionModel = handler.getMetadata("table.selectionModel");
        this.tableModel = handler.getMetadata("table.genericModel");
        this.scrollModel = handler.getMetadata("table.scrollModel");

        if (type == null || selectionModel == null || tableModel == null || scrollModel == null)
            throw new UnsupportedOperationException("Cannot create table editor without type & models");

        tableModel.setConfig(value);

        header.setTableHeader(tableModel);
        tableComponent.setModel(tableModel);
        tableComponent.setSelectionModel(selectionModel);

        scrollPane.getVerticalScrollBar().setModel(scrollModel);

        table.addListener(update);

        return scrollPane;
    }

    @Override
    public boolean stopCellEditing() {
        if (!tableComponent.isEditing() || tableComponent.getCellEditor().stopCellEditing()) {
            setting.removeListener(update);
            return true;
        }
        return false;
    }

    @Override
    public void cancelCellEditing() {
        if (tableComponent.isEditing()) tableComponent.getCellEditor().cancelCellEditing();
        setting.removeListener(update);
    }

    @Override
    public Map<String, Object> getEditorValue() {
        return value;
    }

    private class AddButton extends MainButton {
        AddButton() {
            super(UIUtils.getIcon("add"));
            super.actionColor = UIUtils.GREEN;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(this,
                    "Name (must be unique):",
                    "New element", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            if (value.containsKey(name)) {
                Popups.showMessageAsync("Error", "Name already in use", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Object obj = api.requireInstance(type);
            value.put(name, obj);
            tableModel.updateEntry(name, obj, true);
        }
    }

    private class RemoveButton extends MainButton {
        RemoveButton() {
            super(UIUtils.getIcon("remove"));
            super.actionColor = UIUtils.RED;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> toRemove = Arrays.stream(tableComponent.getSelectedRows())
                    .mapToObj(row -> (String) tableModel.getValueAt(row, 0))
                    .peek(value::remove)
                    .collect(Collectors.toList());
            boolean single = toRemove.size() <= 1;
            toRemove.forEach(r -> tableModel.updateEntry(r, null, single));
            if (!single) tableModel.fireTableDataChanged();
        }
    }

}
