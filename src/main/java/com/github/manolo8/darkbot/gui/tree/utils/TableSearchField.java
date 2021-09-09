package com.github.manolo8.darkbot.gui.tree.utils;

import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.SearchField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import java.util.regex.PatternSyntaxException;

public class TableSearchField<M> extends SearchField {

    private final TableRowSorter<? extends M> sorter;

    private boolean valid;

    public TableSearchField(TableRowSorter<? extends M> sorter, Document document) {
        this.sorter = sorter;
        setDocument(document);
    }

    public TableSearchField(TableRowSorter<? extends M> sorter) {
        this.sorter = sorter;
        update((DocumentEvent) null);
    }

    public void update(DocumentEvent e) {
        if (sorter == null) return;
        try {
            if (sorter instanceof MultiTableRowSorter)
                ((MultiTableRowSorter<?>) sorter).putRowFilter("search", RowFilter.regexFilter("(?i)" + getText(), 0));
            else
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + getText(), 0));
            setValid(true);
        } catch (PatternSyntaxException ex) {
            setValid(false);
        }
    }

    private void setValid(boolean valid) {
        if (this.valid == valid) return; // No change
        this.valid = valid;
        putClientProperty("JComponent.outline", valid ? null : "error");
    }

}