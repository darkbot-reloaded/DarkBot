package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class JSearchField<M> extends SearchField {

    private TableRowSorter<? extends M> sorter;
    private final RowFilter<M, Integer> extraFilter;

    private boolean valid;

    public JSearchField() {
        this.sorter = null;
        this.extraFilter = null;
    }

    public JSearchField(TableRowSorter<? extends M> sorter, RowFilter<M, Integer> extraFilter) {
        this.sorter = sorter;
        this.extraFilter = extraFilter;
        update((DocumentEvent) null);
    }

    public void setSorter(Document document, TableRowSorter<? extends M> sorter) {
        this.sorter = sorter;
        setDocument(document);
    }

    public void update(DocumentEvent e) {
        if (sorter == null) return;
        try {
            sorter.setRowFilter(getFilterFor(RowFilter.regexFilter("(?i)" + getText(), 0)));
            setValid(true);
        } catch (PatternSyntaxException ex) {
            setValid(false);
        }
    }

    private RowFilter<M, Integer> getFilterFor(RowFilter<M, Integer> filter) {
        return extraFilter == null ? filter : RowFilter.andFilter(Arrays.asList(extraFilter, filter));
    }

    private void setValid(boolean valid) {
        if (this.valid == valid) return; // No change
        this.valid = valid;
        putClientProperty("JComponent.outline", valid ? null : "error");
    }

}