package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigBuilder;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.PluginInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenericTableModel<T> extends AbstractTableModel {
    protected Map<String, T> config;

    protected final Column[] columns;
    protected final List<Row<T>> rows = new ArrayList<>();
    protected final Map<String, Row<T>> table = new HashMap<>();

    public GenericTableModel(Class<T> clazz, Map<String, T> config, Lazy<String> modified) {
        this.columns = Stream.concat(Stream.of(clazz), Arrays.stream(clazz.getDeclaredFields()))
                .filter(el -> el.isAnnotationPresent(Option.class))
                .map(Column::new)
                .toArray(Column[]::new);

        setConfig(config);
        if (modified != null) modified.add(n -> updateEntry(n, config.get(n), true));
    }

    public GenericTableModel(PluginAPI api, @Nullable PluginInfo namespace, Class<T> clazz) {
        ConfigBuilder builder = api.requireInstance(ConfigBuilder.class);
        ConfigSetting.Parent<T> parent = builder.of(clazz, "Name", namespace);

        this.columns = Stream.concat(Stream.of(parent), parent.getChildren().values().stream())
                .map(Column::new)
                .toArray(Column[]::new);
    }

    public void setConfig(Map<String, T> config) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setConfig(config));
            return;
        }
        if (config == null || this.config != config) {
            this.config = config;
            rebuildTable();
        } else {
            Set<String> tableNames = this.config.entrySet().stream()
                    .map(e -> updateEntry(e.getKey(), e.getValue(), false))
                    .collect(Collectors.toSet());
            // If table is not the size of the config (after mapping), means something was removed
            if (this.table.size() > tableNames.size()) {
                Iterator<Map.Entry<String, Row<T>>> it = table.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Row<T>> entry = it.next();
                    if (tableNames.contains(entry.getKey())) continue;
                    it.remove();
                    rows.remove(entry.getValue());
                }
            }
            fireTableDataChanged();
        }
    }

    public void rebuildTable() {
        rows.clear();
        table.clear();
        if (config != null) config.forEach((k, v) -> updateEntry(k, v, false));
        fireTableDataChanged();
    }

    public String toTableName(String name) {
        return name;
    }

    public String updateEntry(String name, T data, boolean fireUpdate) {
        // While it would be amazing to optimize this to create insert, update or delete
        // events instead of firing whole data change event, we currently cannot do that.
        // If we use an individual event, the row sorter will receive it two times, making it
        // lose track of how many actual rows are in the model, throwing an exception.
        String tableName = toTableName(name);
        if (data == null) {
            Row<T> r = table.remove(tableName);
            if (r != null) rows.remove(r);
        } else {
            table.compute(tableName, (n, row) -> {
                if (row == null) {
                    row = createRow(tableName, data);
                    rows.add(row);
                    return row;
                }
                return row.update(data);
            });
        }
        if (fireUpdate) fireTableDataChanged();
        return tableName;
    }

    protected Row<T> createRow(String name, T data) {
        return new Row<>(name, data);
    }

    protected void clear() {
        rows.clear();
        table.clear();
        fireTableDataChanged();
    }

    @Override
    public void fireTableDataChanged() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::fireTableDataChanged);
            return;
        }
        super.fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column].name;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columns[column].type;
    }

    public String getToolTipAt(int column) {
        return columns[column].tooltip;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return columns[column].editable;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return getValue(rows.get(row), columns[column]);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        setValue(rows.get(row), columns[column], value);
        ConfigEntity.changed();
    }

    public Row<T> getRow(int row) {
        return rows.get(row);
    }

    protected Object getValue(Row<T> row, Column column) {
        if (column.field == null) return row.name;
        return ReflectionUtils.get(column.field, row.data);
    }

    protected void setValue(Row<T> row, Column column, Object value) {
        Field field = column.field;
        if (field == null) throw new UnsupportedOperationException("Can't edit default column");
        ReflectionUtils.set(field, row.data, value);
    }

    protected static class Column {
        public final String name;
        public final String tooltip;
        public final Field field;
        public final Class<?> type;
        public final boolean editable;

        public Column(AnnotatedElement el) {
            Option op = el.getAnnotation(Option.class);
            this.name = I18n.getOrDefault(op.key(), op.value());
            this.tooltip = Strings.toTooltip(I18n.getOrDefault(op.key() + ".desc", op.description()));
            this.field = el instanceof Field ? (Field) el : null;
            this.type = field == null ? String.class : ReflectionUtils.wrapped(field.getType());
            this.editable = field != null;
        }

        public Column(ConfigSetting<?> config) {
            this.name = config.getName();
            this.tooltip = config.getDescription();
            this.field = config.getMetadata("field");
            this.type = field == null ? String.class : config.getType();
            this.editable = field != null && !Boolean.TRUE.equals(config.getMetadata("readonly"));
        }
    }

    protected static class Row<T> {
        public final String name;
        public T data;

        public Row(String name, T data) {
            this.name = name;
            this.data = data;
        }

        public Row<T> update(T data) {
            this.data = data;
            return this;
        }
    }

}
