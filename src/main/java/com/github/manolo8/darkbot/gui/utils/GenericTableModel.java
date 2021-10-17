package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenericTableModel<T> extends AbstractTableModel {
    protected Map<String, T> config;

    protected final Column[] columns;
    protected final List<Row<T>> rows = new ArrayList<>();
    private final Map<String, Row<T>> table = new HashMap<>();

    public GenericTableModel(Class<T> clazz, Map<String, T> config, Lazy<String> modified) {
        this.config = config;

        this.columns = Stream.concat(Stream.of(clazz), Arrays.stream(clazz.getDeclaredFields()))
                .filter(el -> el.isAnnotationPresent(Option.class))
                .map(Column::new)
                .toArray(Column[]::new);

        if (modified != null) modified.add(n -> updateEntry(n, config.get(n)));
        updateTable();
    }

    public void updateTable() {
        rows.clear();
        table.clear();
        config.forEach(this::updateEntry);
        fireTableDataChanged();
    }

    protected void updateEntry(String name, T data) {
        if (data == null) {
            Row<T> r = table.remove(name);
            if (r != null) rows.remove(r);
        } else {
            table.compute(name, (n, row) -> {
                if (row == null) {
                    row = createRow(name, data);
                    rows.add(row);
                    return row;
                }
                return row.update(data);
            });
        }
        fireTableDataChanged();
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
        return table.size();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > 0;
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

        public Column(AnnotatedElement el) {
            Option op = el.getAnnotation(Option.class);
            this.name = I18n.getOrDefault(op.key(), op.value());
            this.tooltip = Strings.toTooltip(I18n.getOrDefault(op.key() + ".desc", op.description()));
            this.field = el instanceof Field ? (Field) el : null;
            this.type = field == null ? String.class : ReflectionUtils.wrapped(field.getType());
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
