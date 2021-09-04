package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.components.JSearchField;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TableEditor extends JPanel implements OptionEditor<Map<String, Object>> {

    private final PluginAPI api;

    private ConfigSetting<Map<String, Object>> setting;
    private Map<String, Object> value;
    private Class<?> type;


    private final JSearchField<GenericTableModel<Object>> searchField = new JSearchField<>();
    private final AddButton addButton = new AddButton();
    private final RemoveButton removeButton = new RemoveButton();

    private final JScrollBar scrollBar;
    private final JTable tableComponent;
    private final ToolTipHeader header;
    private GenericTableModel<Object> tableModel;

    private final Consumer<Map<String, Object>> update = v -> tableModel.setConfig(v);

    public TableEditor(PluginAPI api) {
        super(new MigLayout("ins 0, gap 0, fill", "[grow][][][]", "[][grow]"));
        this.api = api;

        this.tableComponent = new JTable();
        this.tableComponent.setTableHeader(header = new ToolTipHeader(tableComponent.getColumnModel()));

        JScrollPane scrollPane = new JScrollPane(tableComponent,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollBar = scrollPane.getVerticalScrollBar();

        add(scrollPane, "grow, span, cell 0 1");
        //setOpaque(false);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Map<String, Object>> table) {
        this.setting = table;
        this.value = table.getValue();
        ValueHandler<Map<String, Object>> handler = table.getHandler();

        EnumSet<Table.Controls> controls = handler.getMetadata("table.controls");

        this.type = handler.getMetadata("table.type");
        this.tableModel = handler.getMetadata("table.tableModel");

        TableColumnModel columnModel = handler.getMetadata("table.columnModel");
        ListSelectionModel selectionModel = handler.getMetadata("table.selectionModel");
        BoundedRangeModel scrollModel = handler.getMetadata("table.scrollModel");
        Document searchModel = handler.getMetadata("table.searchModel");
        TableRowSorter<GenericTableModel<Object>> sorter = handler.getMetadata("table.rowSorter");

        if (controls == null || type == null ||
                tableModel == null || columnModel == null || selectionModel == null ||
                scrollModel == null || searchModel == null || sorter == null)
            throw new UnsupportedOperationException("Cannot create table editor without type & models");

        tableModel.setConfig(value);

        header.setTableHeader(tableModel);
        tableComponent.setColumnModel(columnModel);
        tableComponent.setSelectionModel(selectionModel);
        tableComponent.setModel(tableModel);
        tableComponent.setRowSorter(sorter);

        searchField.setSorter(searchModel, sorter);

        scrollBar.setModel(scrollModel);

        table.addListener(update);

        // TODO: allow @Table to customize height
        //  possibly add a hard-coded height based on elements option
        this.setPreferredSize(new Dimension(500, 270));

        checkControl(controls.contains(Table.Controls.SEARCH), searchField, "grow, cell 0 0");
        checkControl(controls.contains(Table.Controls.ADD), addButton, "grow, cell 2 0");
        checkControl(controls.contains(Table.Controls.REMOVE), removeButton, "grow, cell 3 0");

        return this;
    }

    private void checkControl(boolean required, JComponent component, String constraints) {
        int idx = -1;
        for (int i = 0; i < getComponentCount(); i++) {
            if (getComponent(i) == component) {
                idx = i;
                break;
            }
        }
        if (required == (idx != -1)) return;
        if (required) add(component, constraints);
        else remove(idx);
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
