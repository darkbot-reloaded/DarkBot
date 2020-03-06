package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.gui.utils.GeneralDocumentListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class JSearchField<M> extends JTextField {

    private static final Icon SEARCH_ICON = UIUtils.getIcon("search");
    private static final Border MARGIN_BORDER = new EmptyBorder(0, SEARCH_ICON.getIconWidth() + 6, 0, 6);

    private final TableRowSorter<? extends M> sorter;
    private final RowFilter<M, Integer> extraFilter;

    public JSearchField(TableRowSorter<? extends M> sorter, RowFilter<M, Integer> extraFilter) {
        this.sorter = sorter;
        this.extraFilter = extraFilter;
        this.getDocument().addDocumentListener((GeneralDocumentListener) e -> update());
        update();
    }

    private void update() {
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


    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int margin = (getHeight() - SEARCH_ICON.getIconHeight()) / 2;
        SEARCH_ICON.paintIcon(this, graphics, margin, margin);
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(new CompoundBorder(border, MARGIN_BORDER));
    }

}