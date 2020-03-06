package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.TableDoubleEditor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class JNpcInfoTable extends InfoTable<JNpcInfoTable.NpcTableModel, NpcInfo> implements OptionEditor {

    private int filteredMap = -1;
    private JComboBox<Map> mapFilter;

    public JNpcInfoTable(Config.Loot config) {
        super(new NpcTableModel(config), config.NPC_INFOS, config.MODIFIED_NPC, NpcInfo::new);

        super.getComponent().setPreferredSize(new Dimension(550, 270));

        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING),
                new RowSorter.SortKey(2, SortOrder.ASCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        setDefaultEditor(ExtraNpcInfoList.class, new ExtraNpcInfoEditor());
        setDefaultEditor(Double.class, new TableDoubleEditor());

        mapFilter = new JComboBox<>();

        mapFilter.addActionListener(e -> {
            if (mapFilter.getSelectedItem() == null) return;
            filteredMap = ((Map) mapFilter.getSelectedItem()).id;
            getRowSorter().allRowsChanged();
        });
        config.MODIFIED_NPC.add(s -> updateMapList(config.NPC_INFOS.values()));
        updateMapList(config.NPC_INFOS.values());

        mapFilter.setSelectedIndex(0);
        getComponent().add(mapFilter, "grow, cell 1 0");
    }

    private void updateMapList(Collection<NpcInfo> npcInfos) {
        Map map = (Map) mapFilter.getSelectedItem();
        mapFilter.removeAllItems();
        mapFilter.addItem(new Map(-1, "*", false, false));

        Set<Integer> maps = npcInfos.stream().flatMap(n -> n.mapList.stream()).collect(Collectors.toSet());
        StarManager.getAllMaps().stream().filter(m -> m.id >= 0 && maps.contains(m.id)).forEach(mapFilter::addItem);

        mapFilter.setSelectedItem(map);
    }

    @Override
    protected RowFilter<NpcTableModel, Integer> extraFilters() {
        return new NpcMapFilter();
    }

    @Override
    public JComponent getComponent() {
        ((NpcTableModel) getModel()).refreshTable();
        return super.getComponent();
    }

    protected static class NpcTableModel extends GenericTableModel<NpcInfo> {

        private Config.Loot config;
        private boolean grouped;
        private java.util.Map<String, Collection<NpcInfo>> NPC_INFOS = new HashMap<>();

        NpcTableModel(Config.Loot config) {
            super(NpcInfo.class, config.NPC_INFOS, config.MODIFIED_NPC);
            this.config = config;
            this.grouped = config.GROUP_NPCS;
            updateTable();
            config.MODIFIED_NPC.add(n -> updateEntry(n, config.NPC_INFOS.get(n)));
        }

        public void refreshTable() {
            if (grouped != config.GROUP_NPCS) {
                grouped = config.GROUP_NPCS;
                updateTable();
            }
        }

        public void updateTable() {
            NPC_INFOS.clear();
            this.setNumRows(0);
            config.NPC_INFOS.forEach(this::updateEntry);
        }

        protected void updateEntry(String originalName, NpcInfo info) {
            if (config == null) return; // Before setup, just ignore entries
            String name = simplifyName(originalName);

            if (info == null) { // Remove info
                if (!NPC_INFOS.containsKey(name)) return;
                config.NPC_INFOS.entrySet().removeIf(e -> simplifyName(e.getKey()).equals(name));
                updateTable();
                return;
            }

            Collection<NpcInfo> infoGroup;
            if (name.equals(originalName)) infoGroup = Collections.singleton(info);
            else (infoGroup = NPC_INFOS.getOrDefault(name, new ArrayList<>())).add(info);

            if (NPC_INFOS.containsKey(name)) {
                info.copyOf(infoGroup.iterator().next());
                return;
            }
            NPC_INFOS.put(name, infoGroup);
            addRow(new Object[]{name, info.radius, info.priority, info.kill, info.attackKey, info.attackFormation, new ExtraNpcInfoList(infoGroup)});
        }

        private String simplifyName(String name) {
            if (!config.GROUP_NPCS || !name.matches("^[^\\d]+\\d{1,3}$")) return name;

            return name.replaceAll("\\d{1,3}$", " *");
        }

        @Override
        public Class<?> getColumnClass(int column) {
            Class<?> cl = super.getColumnClass(column);
            if (cl == NpcInfo.ExtraNpcInfo.class) return ExtraNpcInfoList.class;
            return cl;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);
            NPC_INFOS.get((String) this.getValueAt(row, 0)).forEach(info -> {
                if (column == 1) info.radius = (Double) value;
                else if (column == 2) info.priority = (Integer) value;
                else if (column == 3) info.kill = (Boolean) value;
                else if (column == 4) info.attackKey = (Character) value;
                else if (column == 5) info.attackFormation = (Character) value;
            });
            ConfigEntity.changed();
        }
    }

    public static class ExtraNpcInfoList {
        Collection<NpcInfo> infos;

        ExtraNpcInfoList(Collection<NpcInfo> infos) {
            this.infos = infos;
        }

        @Override
        public String toString() {
            return infos.iterator().next().extra.toString();
        }
    }

    private class NpcMapFilter extends RowFilter<NpcTableModel, Integer> {
        @Override
        public boolean include(Entry<? extends NpcTableModel, ? extends Integer> entry) {
            if (filteredMap == -1) return true;
            NpcTableModel model = entry.getModel();
            return model.NPC_INFOS.get((String) model.getValueAt(entry.getIdentifier(), 0))
                    .stream().anyMatch(n -> n.mapList.contains(filteredMap));
        }
    }

}
