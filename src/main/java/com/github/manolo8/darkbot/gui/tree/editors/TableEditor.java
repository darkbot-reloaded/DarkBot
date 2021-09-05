package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.extensions.plugins.Plugin;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.components.JSearchField;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.table.ExtraNpcInfoEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableCharEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableCharRenderer;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleRenderer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TableEditor implements OptionEditor<Map<String, Object>> {

    private final PluginAPI api;
    private final Map<ConfigSetting<Map<String, Object>>, JComponent[]> tableMap = new HashMap<>();

    private ConfigSetting<Map<String, Object>> setting;
    private GenericTableModel<Object> tableModel;

    private final Consumer<Map<String, Object>> update = v -> tableModel.setConfig(v);

    public TableEditor(PluginAPI api) {
        this.api = api;
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Map<String, Object>> table) {
        if (this.setting != null) setting.removeListener(update);

        this.setting = table;
        this.tableModel = table.getHandler().getMetadata("table.tableModel");
        if (tableModel == null)
            throw new UnsupportedOperationException("Missing table model");

        this.setting.addListener(update);

        return getComponent(table);
    }

    private JComponent getComponent(ConfigSetting<Map<String, Object>> table) {
        return tableMap.computeIfAbsent(table, this::createTable)[0];
    }

    private JTable getTable(ConfigSetting<Map<String, Object>> table) {
        return (JTable) tableMap.computeIfAbsent(table, this::createTable)[1];
    }

    private JComponent[] createTable(ConfigSetting<Map<String, Object>> setting) {
        ValueHandler<Map<String, Object>> handler = setting.getHandler();

        EnumSet<Table.Controls> controls = handler.getMetadata("table.controls");
        Class<?> type = handler.getMetadata("table.type");

        GenericTableModel<Object> tableModel = handler.getMetadata("table.tableModel");
        TableColumnModel columnModel = handler.getMetadata("table.columnModel");
        ListSelectionModel selectionModel = handler.getMetadata("table.selectionModel");
        BoundedRangeModel scrollModel = handler.getMetadata("table.scrollModel");
        Document searchModel = handler.getMetadata("table.searchModel");
        TableRowSorter<GenericTableModel<Object>> sorter = handler.getMetadata("table.rowSorter");

        if (controls == null || type == null || tableModel == null || columnModel == null ||
                selectionModel == null || scrollModel == null || searchModel == null || sorter == null)
            throw new UnsupportedOperationException("Cannot create table editor without the required models");

        JTable table = new JTable(tableModel, columnModel, selectionModel);
        table.setAutoCreateColumnsFromModel(true);
        table.setTableHeader(new ToolTipHeader(columnModel, tableModel));
        table.setRowSorter(sorter);

        table.setDefaultRenderer(Double.class, new TableDoubleRenderer());
        table.setDefaultRenderer(Character.class, new TableCharRenderer());

        table.setDefaultEditor(Double.class, new TableDoubleEditor());
        table.setDefaultEditor(Character.class, new TableCharEditor());

        // TODO: this editors should be configured via special annotation(s), not hard-hoded here
        table.setDefaultEditor(NpcInfo.ExtraNpcInfo.class, new ExtraNpcInfoEditor());

        columnModel.getColumn(0).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewport(new JViewportFix(table));
        scrollPane.getVerticalScrollBar().setModel(scrollModel);

        if (controls.isEmpty()) return new JComponent[]{scrollPane, table};

        JPanel wrapper = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][][]", "[][grow]"));

        if (controls.contains(Table.Controls.SEARCH))
            wrapper.add(new JSearchField<>(searchModel, sorter), "grow, cell 0 0");
        if (controls.contains(Table.Controls.ADD))
            wrapper.add(new AddButton(api, setting, tableModel, type), "grow, cell 2 0");
        if (controls.contains(Table.Controls.REMOVE))
            wrapper.add(new RemoveButton(setting, table, tableModel), "grow, cell 3 0");

        wrapper.add(scrollPane, "grow, span, cell 0 1");


        // TODO: allow @Table to customize height
        //  possibly add a hard-coded height based on elements option
        wrapper.setPreferredSize(new Dimension(500, 270));

        // Force an initial update on the model
        tableModel.setConfig(setting.getValue());

        return new JComponent[]{wrapper, table};
    }


    @Override
    public boolean stopCellEditing() {
        JTable tableComponent = getTable(setting);
        if (!tableComponent.isEditing() || tableComponent.getCellEditor().stopCellEditing()) {
            setting.removeListener(update);
            return true;
        }
        return false;
    }

    @Override
    public void cancelCellEditing() {
        JTable tableComponent = getTable(setting);
        if (tableComponent.isEditing()) tableComponent.getCellEditor().cancelCellEditing();
        setting.removeListener(update);
    }

    @Override
    public Map<String, Object> getEditorValue() {
        return setting.getValue();
    }

    private static class AddButton extends MainButton {

        private final PluginAPI api;
        private final ConfigSetting<Map<String, Object>> setting;
        private final GenericTableModel<Object> tableModel;
        private final Class<?> type;

        AddButton(PluginAPI api,
                  ConfigSetting<Map<String, Object>> setting,
                  GenericTableModel<Object> tableModel,
                  Class<?> type) {
            super(UIUtils.getIcon("add"));
            super.actionColor = UIUtils.GREEN;
            this.api = api;
            this.setting = setting;
            this.tableModel = tableModel;
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(this,
                    "Name (must be unique):",
                    "New element", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            Map<String, Object> value = setting.getValue();
            if (value == null) return;
            if (value.containsKey(name)) {
                Popups.showMessageAsync("Error", "Name already in use", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Object obj = api.requireInstance(type);
            value.put(name, obj);
            tableModel.updateEntry(name, obj, true);
        }
    }

    private static class RemoveButton extends MainButton {

        private final ConfigSetting<Map<String, Object>> setting;
        private final JTable tableComponent;
        private final GenericTableModel<Object> tableModel;

        RemoveButton(ConfigSetting<Map<String, Object>> setting,
                     JTable table, GenericTableModel<Object> tableModel) {
            super(UIUtils.getIcon("remove"));
            super.actionColor = UIUtils.RED;
            this.setting = setting;
            this.tableComponent = table;
            this.tableModel = tableModel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Map<String, Object> value = setting.getValue();
            if (value == null) return;
            List<String> toRemove = Arrays.stream(tableComponent.getSelectedRows())
                    .mapToObj(row -> (String) tableModel.getValueAt(row, 0))
                    .peek(value::remove)
                    .collect(Collectors.toList());
            toRemove.forEach(r -> tableModel.updateEntry(r, null, false));
            tableModel.fireTableDataChanged();
        }
    }

    /*
     * Removes caching of the size on the viewport side, which returns
     * previous sizes for the table when rows are added and not properly synced.
     */
    private static class JViewportFix extends JViewport {

        public JViewportFix(Component view) {
            setView(view);
        }

        /*
         * BasicScrollPaneUI#syncScrollPaneWithViewport will call this method, we need to give an up-to-date response.
         */
        public Dimension getViewSize() {
            Component view = getView();
            return view == null ? new Dimension(0,0) : view.getPreferredSize();
        }
    }

}
