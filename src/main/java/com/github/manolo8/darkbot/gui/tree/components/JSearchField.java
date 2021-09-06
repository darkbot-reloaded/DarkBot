package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class JSearchField<M> extends SearchField {

    private final TableRowSorter<? extends M> sorter;

    private boolean valid;

    public JSearchField(TableRowSorter<? extends M> sorter, Document document) {
        this.sorter = sorter;
        setDocument(document);
    }

    public JSearchField(TableRowSorter<? extends M> sorter) {
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