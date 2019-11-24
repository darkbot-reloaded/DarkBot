package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class GenericTableModel<T> extends DefaultTableModel {
    private final Field[] FIELDS;
    private final String[] TOOLTIPS;
    private final Class[] TYPES;

    private Map<String, T> table = new HashMap<>();

    public GenericTableModel(Class<T> clazz, Map<String, T> config, Lazy<String> modified) {
        super(getNames(clazz), 0);

        FIELDS = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .toArray(Field[]::new);

        TOOLTIPS = prepend(clazz, Arrays.stream(FIELDS))
                .map(annotated -> annotated.getAnnotation(Option.class))
                .map(op -> I18n.getOrDefault(op.key() + ".desc", op.description()))
                .map(s -> s.isEmpty() ? null : s)
                .toArray(String[]::new);

        TYPES = prepend(String.class, Arrays.stream(FIELDS).map(Field::getType)
                .map(ReflectionUtils::wrapped)).toArray(Class[]::new);

        config.forEach(this::updateEntry);
        if (modified != null) modified.add(n -> updateEntry(n, config.get(n)));
    }

    public String getToolTipAt(int column) {
        return TOOLTIPS[column];
    }

    protected void updateEntry(String name, T data) {
        if (data == null) {
            if (table.containsKey(name)) removeEntry(name);
        } else {
            if (table.containsKey(name)) updateEntryRow(name, data);
            else addEntry(name, data);
        }
    }

    private void addEntry(String name, T data) {
        table.put(name, data);
        addRow(prepend(name, Arrays.stream(FIELDS).map(f -> ReflectionUtils.get(f, data))).toArray(Object[]::new));
    }

    private void updateEntryRow(String name, T data) {
        table.put(name, data);
        for (int row = 0; row < getRowCount(); row++) {
            if (!getValueAt(row, 0).equals(name)) continue;
            for (int field = 0; field < FIELDS.length;) {
                setValueAt(ReflectionUtils.get(FIELDS[field], data), row, ++field);
            }
        }
    }

    private void removeEntry(String name) {
        table.remove(name);

        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, 0).equals(name)) removeRow(i--);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > 0;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return TYPES[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        super.setValueAt(value, row, column);

        if (!table.isEmpty()) { // Npc table ignore this table
            ReflectionUtils.set(FIELDS[column - 1], table.get((String) this.getValueAt(row, 0)), value);
            ConfigEntity.changed();
        }
    }

    private static <T> Stream<T> prepend(T type, Stream<T> stream) {
        return Stream.concat(Stream.of(type), stream);
    }

    private static String[] getNames(Class clazz) {
        return prepend(clazz, Arrays.stream(clazz.getDeclaredFields()))
                .map(annotated -> annotated.getAnnotation(Option.class))
                .filter(Objects::nonNull)
                .map(op -> I18n.getOrDefault(op.key(), op.value()))
                .toArray(String[]::new);
    }

}
