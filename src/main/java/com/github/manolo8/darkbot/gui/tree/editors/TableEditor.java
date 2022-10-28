package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.utils.TableSearchField;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.ToolTipHeader;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.table.TableCharEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableCharRenderer;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleEditor;
import com.github.manolo8.darkbot.gui.utils.table.TableDoubleRenderer;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.util.OptionEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableEditor implements OptionEditor<Map<String, Object>> {

    private final PluginAPI api;
    private static final Map<ConfigSetting<Map<String, Object>>, JComponent[]> tableMap = new WeakHashMap<>();

    private ConfigSetting<Map<String, Object>> setting;
    private GenericTableModel<Object> tableModel;

    private final Consumer<Map<String, Object>> update = v -> tableModel.setConfig(v);

    public TableEditor(PluginAPI api) {
        this.api = api;
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Map<String, Object>> table) {
        throw new UnsupportedOperationException("isEditor is required");
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Map<String, Object>> table, boolean isEditor) {
        if (isEditor && this.setting != null) setting.removeListener(update);

        this.setting = table;
        this.tableModel = table.getMetadata("table.tableModel");
        if (tableModel == null)
            throw new UnsupportedOperationException("Missing table model");

        if (isEditor) this.setting.addListener(update);

        // Update model with new data if applicable
        this.tableModel.setConfig(table.getValue());

        return getComponent(table);
    }

    private JComponent getComponent(ConfigSetting<Map<String, Object>> table) {
        return tableMap.computeIfAbsent(table, this::createTable)[0];
    }

    private JTable getTable(ConfigSetting<Map<String, Object>> table) {
        return (JTable) tableMap.computeIfAbsent(table, this::createTable)[1];
    }

    private JComponent[] createTable(ConfigSetting<Map<String, Object>> setting) {

        Table.Control[] controls = setting.getMetadata("table.controls");
        Class<? extends Table.ControlBuilder<Object>>[] custom = setting.getMetadata("table.customControls");
        Class<? extends Table.Decorator<Object>>[] decorators = setting.getMetadata("table.decorators");
        Class<?> type = setting.getMetadata("table.type");

        GenericTableModel<Object> tableModel = setting.getMetadata("table.tableModel");
        if (controls == null || custom == null || decorators == null || tableModel == null || type == null)
            throw new UnsupportedOperationException("Cannot create table editor without the required metadata");


        TableColumnModel columnModel = setting.getOrCreateMetadata("table.columnModel", DefaultTableColumnModel::new);
        ListSelectionModel selectionModel = setting.getOrCreateMetadata("table.selectionModel", DefaultListSelectionModel::new);
        BoundedRangeModel scrollModel = setting.getOrCreateMetadata("table.scrollModel", DefaultBoundedRangeModel::new);
        TableRowSorter<TableModel> sorter = setting.getOrCreateMetadata("table.rowSorter", () -> new MultiTableRowSorter<>(tableModel));

        JTable table = new JTable(tableModel, columnModel, selectionModel);
        table.setAutoCreateColumnsFromModel(true);
        table.setTableHeader(new ToolTipHeader(columnModel, tableModel));
        table.setRowSorter(sorter);

        table.setDefaultRenderer(Double.class, new TableDoubleRenderer());
        table.setDefaultRenderer(Character.class, new TableCharRenderer());

        table.setDefaultEditor(Double.class, new TableDoubleEditor());
        table.setDefaultEditor(Character.class, new TableCharEditor());

        // Default to first column having 200 width.
        // Implementers may override by using a custom decorator
        columnModel.getColumn(0).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewport(new JViewportFix(table));
        scrollPane.getVerticalScrollBar().setModel(scrollModel);

        JPanel wrapper = null;
        if (controls.length > 0) {
            String controlFields = IntStream.range(1, controls.length)
                    .mapToObj(i -> "[]").collect(Collectors.joining(""));
            wrapper = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]" + controlFields, "[][grow]"));

            for (int i = 0, j = 0; i < controls.length; i++) {
                Table.Control control = controls[i];
                JComponent component;
                if (control == Table.Control.SEARCH)
                    component = new TableSearchField<>(sorter,
                            setting.getOrCreateMetadata("table.searchModel", PlainDocument::new));
                else if (control == Table.Control.ADD)
                    component = new AddButton(api, this.setting, tableModel, type);
                else if (control == Table.Control.REMOVE)
                    component = new RemoveButton(this.setting, table, tableModel);
                else if (control == Table.Control.CUSTOM) {
                    component = api.requireInstance(custom[j++]).create(table, this.setting);
                } else
                    throw new UnsupportedOperationException("Control not supported: " + control);

                wrapper.add(component, "grow, cell " + i + " 0");
            }

            wrapper.add(scrollPane, "grow, span, cell 0 1");
        }
        JComponent outer = wrapper == null ? scrollPane : wrapper;

        // Set a default table size, implementers may override using a decorator
        outer.setPreferredSize(new Dimension(500, 270));

        for (Class<? extends Table.Decorator<Object>> decorator : decorators)
                api.requireInstance(decorator).handle(table, scrollPane, wrapper, this.setting);

        return new JComponent[]{outer, table};
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
                JOptionPane.showMessageDialog(this, "Name already in use", "Error", JOptionPane.ERROR_MESSAGE);
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
            Set<String> tableNames = Arrays.stream(tableComponent.getSelectedRows())
                    .mapToObj(row -> (String) tableComponent.getValueAt(row, 0))
                    .collect(Collectors.toSet());

            List<String> toRemove = value.keySet().stream()
                    .filter(name -> tableNames.contains(tableModel.toTableName(name)))
                    .collect(Collectors.toList());

            tableComponent.getSelectionModel().clearSelection();
            toRemove.forEach(value::remove);
            setting.setValue(value);
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
