package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.regex.PatternSyntaxException;

public class JSearchField extends JTextField {
    private static final Color ERROR = Color.decode("#6E2B28");

    private final TableRowSorter<? extends DefaultTableModel> sorter;

    public JSearchField(TableRowSorter<? extends DefaultTableModel> sorter) {
        this.sorter = sorter;
        putClientProperty("ConfigTree", true);
        this.getDocument().addDocumentListener((GeneralDocumentListener) e -> update());
        setPreferredSize(new Dimension(30, 16));
    }

    private void update() {
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + getText(), 0));

            setBackground(null);
        } catch (PatternSyntaxException e) {
            setBackground(ERROR);
        }
    }

}