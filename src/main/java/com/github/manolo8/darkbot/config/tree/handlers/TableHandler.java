package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumSet;
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
        metadata.put("table.controls", EnumSet.copyOf(Arrays.asList(controls)));
        metadata.put("table.type", type);

        GenericTableModel<?> tableModel = new GenericTableModel<>(type);
        metadata.put("table.selectionModel", new DefaultListSelectionModel());
        metadata.put("table.tableModel", tableModel);
        metadata.put("table.columnModel", new DefaultTableColumnModel());
        metadata.put("table.searchModel", new PlainDocument());
        metadata.put("table.rowSorter", new TableRowSorter<>(tableModel));

        metadata.put("table.scrollModel", new DefaultBoundedRangeModel() {
            @Override
            public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
                // There's an odd issue where BasicScrollPaneUI#syncScrollPaneWithViewport will update
                // the extent based on the extentSize.height, and it turns out to be 0 when reusing the
                // model in both renderer & editor.
                // The easiest fix is to just ignore keep the extent.
                if (newExtent == 0) newExtent = this.getExtent();
                super.setRangeProperties(newValue, newExtent, newMin, newMax, adjusting);
            }
        });
    }

}
