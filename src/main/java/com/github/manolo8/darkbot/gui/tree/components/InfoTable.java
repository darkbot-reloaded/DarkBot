package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.TableCharEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public abstract class InfoTable<T extends TableModel> extends JTable implements OptionEditor {
    private static final TableCharEditor CHAR_EDITOR = new TableCharEditor();

    private JComponent component;

    InfoTable(T model) {
        super(model);
        getColumnModel().getColumn(0).setPreferredWidth(200);
        putClientProperty("ConfigTree", true);
        setDefaultEditor(Character.class, CHAR_EDITOR);

        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);

        TableRowSorter<T> sorter = new TableRowSorter<>(model);
        setRowSorter(sorter);

        component = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow][]", "[][grow]"));

        component.add(new JSearchField<>(sorter, extraFilters()), "grow, wrap");
        component.add(new JScrollPane(this), "grow, span");

        component.setPreferredSize(new Dimension(550, 270));
    }

    protected RowFilter<T, Integer> extraFilters() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void edit(ConfigField ignore) {}

}
