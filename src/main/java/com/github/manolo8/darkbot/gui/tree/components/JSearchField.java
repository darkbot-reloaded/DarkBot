package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class JSearchField<M> extends JTextField {
    private static final Color ERROR = Color.decode("#6E2B28");

    private final TableRowSorter<? extends M> sorter;
    private final RowFilter<M, Integer> extraFilter;

    public JSearchField(TableRowSorter<? extends M> sorter, RowFilter<M, Integer> extraFilter) {
        this.sorter = sorter;
        this.extraFilter = extraFilter;
        putClientProperty("ConfigTree", true);
        putClientProperty("JTextField.variant", "search");
        this.getDocument().addDocumentListener((GeneralDocumentListener) e -> update());
        setPreferredSize(new Dimension(30, 16));
        update();
    }

    private void update() {
        try {
            sorter.setRowFilter(getFilterFor(RowFilter.regexFilter("(?i)" + getText(), 0)));

            setBackground(null);
        } catch (PatternSyntaxException e) {
            setBackground(ERROR);
        }
    }

    private RowFilter<M, Integer> getFilterFor(RowFilter<M, Integer> filter) {
        return extraFilter == null ? filter : RowFilter.andFilter(Arrays.asList(extraFilter, filter));
    }

}