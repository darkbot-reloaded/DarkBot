package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.HashMap;
import java.util.Map;

public class MultiTableRowSorter<M extends TableModel> extends TableRowSorter<M> {

    private final Map<String, RowFilter<? super M, Integer>> filters = new HashMap<>();

    public MultiTableRowSorter() {
    }

    public MultiTableRowSorter(M model) {
        super(model);
    }

    public void putRowFilter(String key, RowFilter<? super M, Integer> filter) {
        RowFilter<? super M, Integer> old = filter == null ? filters.remove(key) : filters.put(key, filter);
        if (old == filter) return;
        setRowFilter(RowFilter.andFilter(filters.values()));
    }
}
