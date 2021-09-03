package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class TableHandler extends DefaultHandler<Object> {

    public static TableHandler of(Field field) {
        Table table = field.getAnnotation(Table.class);
        Class<?> type = getTableType(field.getGenericType());
        if (type == null)
            throw new UnsupportedOperationException("Table configuration must be a parameterized type Map<String, YourType>");
        return new TableHandler(field, table.controls(), type);
    }

    private static Class<?> getTableType(Type generic) {
        if (!(generic instanceof ParameterizedType)) return null;
        ParameterizedType type = (ParameterizedType) generic;

        Type rawType = type.getRawType();
        if (!(rawType instanceof Class) || !Map.class.isAssignableFrom((Class<?>) rawType)) return null;

        Type[] args = type.getActualTypeArguments();
        if (args.length != 2 || !(args[0] instanceof Class) || !(args[1] instanceof Class)) return null;
        if (!String.class.isAssignableFrom((Class<?>) args[0])) return null;

        return (Class<?>) args[1];
    }

    public TableHandler(@Nullable Field field, Table.Controls[] controls, Class<?> type) {
        super(field);
        metadata.put("isTable", true);
        metadata.put("table.controls", controls);
        metadata.put("table.type", type);

        metadata.put("table.selectionModel", new DefaultListSelectionModel());
        metadata.put("table.genericModel", new GenericTableModel<>(type));

        // TODO: investigate proper values, these are taken from JScrollPane creating default bars, but they are off.
        metadata.put("table.scrollModel", new DefaultBoundedRangeModel(0, 10, 0, 100));
    }

}
