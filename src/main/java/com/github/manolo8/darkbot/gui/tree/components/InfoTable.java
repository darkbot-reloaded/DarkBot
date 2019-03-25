package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.TableCharEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class InfoTable extends JTable implements OptionEditor {
    private static final TableCharEditor CHAR_EDITOR = new TableCharEditor();

    private JComponent component;

    InfoTable(TableModel model) {
        super(model);
        getColumnModel().getColumn(0).setPreferredWidth(200);
        putClientProperty("ConfigTree", true);
        setDefaultEditor(Character.class, CHAR_EDITOR);

        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) getModel());
        setRowSorter(sorter);

        JPanel panel = new JPanel(new GridBagLayout()) {
            public void setPreferredSize(Dimension preferredSize) {
                super.setPreferredSize(new Dimension(500, 300));
            }
        };

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;

        panel.add(new JSearchField(sorter), c);

        c.weighty = c.gridy = 1;
        panel.add(new JScrollPane(this), c);

        component = panel;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void edit(ConfigField ignore) {}

}
