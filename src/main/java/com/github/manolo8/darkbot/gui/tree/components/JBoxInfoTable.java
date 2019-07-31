package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class JBoxInfoTable extends InfoTable<JBoxInfoTable.BoxTableModel> implements OptionEditor {

    public JBoxInfoTable(Config.Collect collect) {
        super(new BoxTableModel(collect.BOX_INFOS, collect.ADDED_BOX));

        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(1, SortOrder.DESCENDING),
                new RowSorter.SortKey(3, SortOrder.ASCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));
    }

    protected static class BoxTableModel extends DefaultTableModel {
        private static final Class[] TYPES = new Class[]{String.class, Boolean.class, Integer.class, Integer.class};

        private Map<String, BoxInfo> BOX_INFOS;

        BoxTableModel(Map<String, BoxInfo> BOX_INFOS, Lazy<String> added) {
            super(new String[]{"Name", "Collect", "Wait (ms)", "Priority"}, 0);
            (this.BOX_INFOS = BOX_INFOS).forEach(this::addEntry);
            added.add(n -> addEntry(n, BOX_INFOS.get(n)));
        }

        private void addEntry(String name, BoxInfo info) {
            addRow(new Object[]{name, info.collect, info.waitTime, info.priority});
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return TYPES[column];
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);
            BoxInfo info = BOX_INFOS.get((String) this.getValueAt(row, 0));
            if (column == 1) info.collect = (Boolean) value;
            else if (column == 2) info.waitTime = (Integer) value;
            else if (column == 3) info.priority = (Integer) value;

            ConfigEntity.changed();
        }
    }

}
