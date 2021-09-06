package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.PlainDocument;
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

        Class<? extends TableModel> modelType = table.customModel();
        TableModel model = modelType == TableModel.class ?
                new GenericTableModel<>(type) : ReflectionUtils.createInstance(modelType);

        return new TableHandler(field, table.controls(), table.customControls(), model, type);
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

    public TableHandler(@Nullable Field field,
                        Table.Control[] controls,
                        Class<? extends Table.ControlBuilder>[] custom,
                        TableModel model,
                        Class<?> type) {
        super(field);
        metadata.put("isTable", true);
        metadata.put("table.controls", controls);
        metadata.put("table.customControls", custom);
        metadata.put("table.type", type);

        metadata.put("table.selectionModel", new DefaultListSelectionModel());
        metadata.put("table.tableModel", model);
        metadata.put("table.columnModel", new DefaultTableColumnModel());
        metadata.put("table.searchModel", new PlainDocument());
        metadata.put("table.rowSorter", new MultiTableRowSorter<>(model));

        metadata.put("table.scrollModel", new DefaultBoundedRangeModel());
    }

}
