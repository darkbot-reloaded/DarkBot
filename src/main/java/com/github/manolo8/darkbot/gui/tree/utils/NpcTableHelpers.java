package com.github.manolo8.darkbot.gui.tree.utils;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.gui.utils.GeneralListDataListener;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.utils.ReflectionUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Table;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NpcTableHelpers {

    public static class NpcTableModel extends GenericTableModel<NpcInfo> {

        private boolean grouped = true;

        public NpcTableModel() {
            super(NpcInfo.class);
            rebuildTable();
        }

        public void setGrouped(boolean grouped) {
            if (this.grouped == grouped) return;
            this.grouped = grouped;
            rebuildTable();
        }

        @Override
        protected Row<NpcInfo> createRow(String name, NpcInfo data) {
            return new NpcTableModel.NpcRow(name, data);
        }

        @Override
        public String toTableName(String name) {
            return grouped ? Strings.simplifyName(name) : name;
        }

        @Override
        protected void setValue(Row<NpcInfo> row, Column column, Object value) {
            Field field = column.field;
            if (field == null) throw new UnsupportedOperationException("Can't edit default column");

            if (value instanceof NpcInfo.ExtraNpcInfo) {
                NpcInfo.ExtraNpcInfo extra = (NpcInfo.ExtraNpcInfo) value;

                for (NpcInfo info : ((NpcTableModel.NpcRow) row).getInfos())
                    info.extra.copy(extra);
            } else {
                for (NpcInfo info : ((NpcTableModel.NpcRow) row).getInfos())
                    ReflectionUtils.set(field, info, value);
            }

            ConfigEntity.changed();
        }

        public static class NpcRow extends GenericTableModel.Row<NpcInfo> {

            private Collection<NpcInfo> infos;

            public NpcRow(String name, NpcInfo data) {
                super(name, data);
                this.infos = Collections.singleton(data);
            }

            @Override
            public NpcTableModel.NpcRow update(NpcInfo info) {
                if (infos.contains(info)) return this;

                if (infos instanceof Set) infos = new ArrayList<>(infos);

                info.copyOf(data);
                infos.add(info);
                return this;
            }

            public Collection<NpcInfo> getInfos() {
                return this.infos;
            }

            public NpcInfo getInfo() {
                return this.data;
            }
        }
    }


    public static class MapPickerBuilder implements Table.ControlBuilder<NpcInfo> {
        private static final String DEFAULT_ALL = "*";

        private final ConfigAPI configAPI;
        private final StarSystemAPI starSystemAPI;

        public MapPickerBuilder(ConfigAPI configAPI, StarSystemAPI starSystemAPI) {
            this.configAPI = configAPI;
            this.starSystemAPI = starSystemAPI;
        }

        @Override
        public JComponent create(JTable table, ConfigSetting<Map<String, NpcInfo>> setting) {
            ValueHandler<Map<String, NpcInfo>> handler = setting.getHandler();

            NpcTableModel tableModel = handler.getMetadata("table.tableModel");
            MultiTableRowSorter<NpcTableModel> sorter = handler.getMetadata("table.rowSorter");
            if (tableModel == null || sorter == null)
                throw new UnsupportedOperationException("Required metadata missing");

            ComboBoxModel<String> model = handler.getOrCreateMetadata("table.mapFilter", () -> {
                // Register the toggle to set grouped or not based on group config
                ConfigSetting<Boolean> group = configAPI.requireConfig(setting.getParent(), "group_npcs");
                group.addListener(tableModel::setGrouped);
                tableModel.setGrouped(group.getValue());

                DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
                m.addElement(DEFAULT_ALL);
                m.setSelectedItem(DEFAULT_ALL);

                // Update map list when NPCs are added or removed
                Consumer<Map<String, NpcInfo>> listener = infos -> {
                    Object selected = m.getSelectedItem();
                    m.removeAllElements();
                    m.addElement(DEFAULT_ALL);
                    Set<Integer> maps = infos.values().stream()
                            .flatMap(n -> n.mapList.stream()).collect(Collectors.toSet());
                    starSystemAPI.getMaps().stream()
                            .filter(map -> map.getId() >= 0 && maps.contains(map.getId()))
                            .map(GameMap::getName).map(Strings::simplifyName)
                            .distinct()
                            .forEach(m::addElement);
                    m.setSelectedItem(selected);
                };
                setting.addListener(listener);
                listener.accept(setting.getValue()); // Update list

                // Update filter when selection changes
                Set<Integer> selectedMaps = new HashSet<>();
                m.addListDataListener(new GeneralListDataListener() {
                    @Override
                    public void contentsChanged(ListDataEvent e) {
                        selectedMaps.clear();
                        String selected = (String) m.getSelectedItem();
                        if (selected == null) return;
                        if (!selected.equals(DEFAULT_ALL))
                            starSystemAPI.getMaps().stream()
                                    .filter(m -> m.getId() >= 0 && selected.equals(Strings.simplifyName(m.getName())))
                                    .mapToInt(GameMap::getId)
                                    .forEach(selectedMaps::add);

                        sorter.allRowsChanged();
                    }
                });

                // Register selection of maps as a filter
                sorter.putRowFilter("map", new RowFilter<NpcTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends NpcTableModel, ? extends Integer> entry) {
                        if (selectedMaps.isEmpty()) return true;
                        NpcTableModel.NpcRow row = (NpcTableModel.NpcRow) entry.getModel().getRow(entry.getIdentifier());
                        return row.getInfos().stream().anyMatch(npc -> !Collections.disjoint(npc.mapList, selectedMaps));
                    }
                });

                return m;
            });

            return new JComboBox<>(model);
        }
    }

}
