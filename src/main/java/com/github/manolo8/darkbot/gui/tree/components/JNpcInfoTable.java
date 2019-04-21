package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Arrays;
import java.util.Map;

public class JNpcInfoTable extends InfoTable implements OptionEditor {

    private int filteredMap = -1;

    public JNpcInfoTable(Config.Loot config) {
        super(new NpcTableModel(config.NPC_INFOS, config.ADDED_NPC));
        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        OptionList<Integer> options = new StarManager.MapList(true);
        JComboBox mapFilter = new JComboBox<>(options);
        mapFilter.addActionListener(e -> {
            filteredMap = options.getValue((String) mapFilter.getSelectedItem());
            getRowSorter().allRowsChanged();
        });
        mapFilter.setSelectedIndex(0);
        getComponent().add(mapFilter, "cell 1 0");
    }

    @Override
    protected RowFilter extraFilters() {
        return new NpcMapFilter();
    }

    private static class NpcTableModel extends DefaultTableModel {
        private static final Class[] TYPES = new Class[]{String.class, Double.class, Integer.class, Boolean.class, Boolean.class, Character.class, Boolean.class};

        private Map<String, NpcInfo> NPC_INFOS;

        NpcTableModel(Map<String, NpcInfo> NPC_INFOS, Lazy<String> added) {
            super(new Object[]{"Name", "Radius", "Priority", "Kill", "No circle", "Ammo Key", "Ign. own"}, 0);
            (this.NPC_INFOS = NPC_INFOS).forEach(this::addEntry);
            added.add(n -> addEntry(n, NPC_INFOS.get(n)));
        }

        private void addEntry(String name, NpcInfo info) {
            addRow(new Object[]{name, info.radius, info.priority, info.kill, info.noCircle, info.attackKey, info.ignoreAttacked});
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
            super.setValueAt(value, row, column);
            NpcInfo info = NPC_INFOS.get((String) this.getValueAt(row, 0));
            if (column == 1) info.radius = (Double) value;
            else if (column == 2) info.priority = (Integer) value;
            else if (column == 3) info.kill = (Boolean) value;
            else if (column == 4) info.noCircle = (Boolean) value;
            else if (column == 5) info.attackKey = (Character) value;
            else if (column == 6) info.ignoreAttacked = (Boolean) value;
        }
    }

    private class NpcMapFilter extends RowFilter<NpcTableModel, Integer> {
        @Override
        public boolean include(Entry<? extends NpcTableModel, ? extends Integer> entry) {
            if (filteredMap == -1) return true;
            NpcTableModel model = entry.getModel();
            return model.NPC_INFOS.get((String) model.getValueAt(entry.getIdentifier(), 0)).mapList.contains(filteredMap);
        }
    }

}
