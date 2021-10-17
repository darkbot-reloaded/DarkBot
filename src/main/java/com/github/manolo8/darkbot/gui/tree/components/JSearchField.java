package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class JSearchField<M> extends SearchField {

    private final TableRowSorter<? extends M> sorter;
    private final RowFilter<M, Integer> extraFilter;

    public JSearchField(TableRowSorter<? extends M> sorter, RowFilter<M, Integer> extraFilter) {
        super(null);
        this.sorter = sorter;
        this.extraFilter = extraFilter;
        update();
    }

    protected void update() {
        try {
            sorter.setRowFilter(getFilterFor(RowFilter.regexFilter("(?i)" + getText(), 0)));

            setBackground(null);
        } catch (PatternSyntaxException e) {
            setBackground(UIUtils.RED);
        }
    }

    private RowFilter<M, Integer> getFilterFor(RowFilter<M, Integer> filter) {
        return extraFilter == null ? filter : RowFilter.andFilter(Arrays.asList(extraFilter, filter));
    }

}