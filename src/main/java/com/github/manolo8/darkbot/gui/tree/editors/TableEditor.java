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

        Table.Control[] controls = handler.getMetadata("table.controls");
        Class<? extends Table.ControlBuilder<Object>>[] custom = handler.getMetadata("table.customControls");
        Class<?> type = handler.getMetadata("table.type");

        GenericTableModel<Object> tableModel = handler.getMetadata("table.tableModel");
        TableColumnModel columnModel = handler.getMetadata("table.columnModel");
        ListSelectionModel selectionModel = handler.getMetadata("table.selectionModel");
        BoundedRangeModel scrollModel = handler.getMetadata("table.scrollModel");
        Document searchModel = handler.getMetadata("table.searchModel");
        TableRowSorter<GenericTableModel<Object>> sorter = handler.getMetadata("table.rowSorter");

        if (controls == null || custom == null || type == null ||
                tableModel == null || columnModel == null || selectionModel == null ||
                scrollModel == null || searchModel == null || sorter == null)
            throw new UnsupportedOperationException("Cannot create table editor without the required models");

        JTable table = new JTable(tableModel, columnModel, selectionModel);
        table.setAutoCreateColumnsFromModel(true);
        table.setTableHeader(new ToolTipHeader(columnModel, tableModel));
        table.setRowSorter(sorter);

        table.setDefaultRenderer(Double.class, new TableDoubleRenderer());
        table.setDefaultRenderer(Character.class, new TableCharRenderer());

        table.setDefaultEditor(Double.class, new TableDoubleEditor());
        table.setDefaultEditor(Character.class, new TableCharEditor());

        // TODO: this editors should be configured via special annotation(s), not hard-coded here
        table.setDefaultEditor(NpcInfo.ExtraNpcInfo.class, new ExtraNpcInfoEditor());

        // TODO: allow preferred widths configured via @Table, as well as sort orders
        columnModel.getColumn(0).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewport(new JViewportFix(table));
        scrollPane.getVerticalScrollBar().setModel(scrollModel);

        // Simple table, no controls, no need for extra fuzz
        if (controls.length == 0) {
            // TODO: customize size via @Table
            scrollPane.setPreferredSize(new Dimension(500, 270));
            return new JComponent[]{scrollPane, table};
        }

        JPanel wrapper = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][][][]", "[][grow]"));

        for (int i = 0, j = 0; i < controls.length; i++) {
            Table.Control control = controls[i];
            JComponent component;
            if (control == Table.Control.SEARCH)
                component = new JSearchField<>(sorter, searchModel);
            else if (control == Table.Control.ADD)
                component = new AddButton(api, setting, tableModel, type);
            else if (control == Table.Control.REMOVE)
                component = new RemoveButton(setting, table, tableModel);
            else if (control == Table.Control.CUSTOM) {
                component = api.requireInstance(custom[j++]).create(table, setting);
            } else {
                throw new UnsupportedOperationException("Control not supported: " + control);
            }

            wrapper.add(component, "grow, cell " + i + " 0");
        }

        wrapper.add(scrollPane, "grow, span, cell 0 1");


        // TODO: customize size via @Table
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
