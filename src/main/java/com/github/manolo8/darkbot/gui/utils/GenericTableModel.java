package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.table.DefaultTableModel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class GenericTableModel<T> extends DefaultTableModel {
    private final Class[] TYPES;
    private final Field[] FIELDS;

    private Map<String, T> table = new HashMap<>();

    public GenericTableModel(Class<T> clazz, Map<String, T> config, Lazy<String> modified) {
        super(getNames(clazz), 0);

        FIELDS = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getAnnotation(Option.class) != null)
                .toArray(Field[]::new);

        TYPES = prepend(String.class, Arrays.stream(FIELDS).map(Field::getType)
                .map(ReflectionUtils::wrapped)).toArray(Class[]::new);

        config.forEach(this::updateEntry);
        if (modified != null) modified.add(n -> updateEntry(n, config.get(n)));
    }

    protected void updateEntry(String name, T data) {
        if (table.containsKey(name) && table.get(name) != data) removeEntry(name);
        if (data != null && !table.containsKey(name)) addEntry(name, data);
    }

    protected void addEntry(String name, T data) {
        if (table.containsKey(name)) return; // Already in table
        table.put(name, data);
        addRow(prepend(name, Arrays.stream(FIELDS).map(f -> ReflectionUtils.get(f, data))).toArray(Object[]::new));
    }

    protected void removeEntry(String name) {
        if (!table.containsKey(name)) return; // Not in table
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
        return prepend((Option) clazz.getAnnotation(Option.class),
                Arrays.stream(clazz.getDeclaredFields()).map(f -> f.getAnnotation(Option.class)))
                .filter(Objects::nonNull)
                .map(Option::value)
                .toArray(String[]::new);
    }

}
