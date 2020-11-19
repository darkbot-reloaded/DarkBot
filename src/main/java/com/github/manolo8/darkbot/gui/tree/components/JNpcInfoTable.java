package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.table.ExtraNpcInfoEditor;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JNpcInfoTable extends InfoTable<JNpcInfoTable.NpcTableModel, NpcInfo> implements OptionEditor {

    private JMapPicker mapPicker;
    private NpcMapFilter mapFilter;

    public JNpcInfoTable(Config.Loot config) {
        super(new NpcTableModel(config), config.NPC_INFOS, config.MODIFIED_NPC, NpcInfo::new);

        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING),
                new RowSorter.SortKey(2, SortOrder.ASCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        setDefaultEditor(NpcInfo.ExtraNpcInfo.class, new ExtraNpcInfoEditor());

        config.MODIFIED_NPC.add(s -> mapPicker.update(config.NPC_INFOS.values()));
        mapPicker.update(config.NPC_INFOS.values());

        getComponent().add(mapPicker, "grow, cell 1 0");
    }

    @Override
    protected RowFilter<NpcTableModel, Integer> extraFilters() {
        if (mapPicker == null) {
            mapPicker = new JMapPicker(maps -> getRowSorter().allRowsChanged());
            mapFilter = new NpcMapFilter(mapPicker.getSelected());
        }
        return mapFilter;
    }

    @Override
    public JComponent getComponent() {
        ((NpcTableModel) getModel()).refreshTable();
        return super.getComponent();
    }

    private static String simplifyName(String name) {
        if (!name.matches("^[^\\d]+\\d{1,3}$")) return name;
        return name.replaceAll("\\d{1,3}$", " *");
    }

    protected static class NpcTableModel extends GenericTableModel<NpcInfo> {

        private final Config.Loot config;
        private boolean grouped;

        NpcTableModel(Config.Loot config) {
            super(NpcInfo.class, config.NPC_INFOS, config.MODIFIED_NPC);
            this.config = config;
            this.grouped = config.GROUP_NPCS;
            updateTable();
            config.MODIFIED_NPC.add(n -> updateEntry(n, config.NPC_INFOS.get(n)));
        }

        public void refreshTable() {
            if (grouped != (grouped = config.GROUP_NPCS)) updateTable();
        }

        @Override
        protected Row createRow(String name, NpcInfo data) {
            return new NpcRow(name, data);
        }

        protected void updateEntry(String name, NpcInfo info) {
            if (config == null) return; // Before setup, just ignore entries
            super.updateEntry(config.GROUP_NPCS ? simplifyName(name) : name, info);
        }

        @Override
        protected Object getValue(GenericTableModel.Row row, GenericTableModel.Column column) {
            if (column.field == null) return row.name;

            NpcRow r = (NpcRow) row;
            return ReflectionUtils.get(column.field, r.getInfo());
        }

        @Override
        protected void setValue(Row row, Column column, Object value) {
            Field field = column.field;
            if (field == null) throw new UnsupportedOperationException("Can't edit default column");

            if (value instanceof NpcInfo.ExtraNpcInfo) {
                NpcInfo.ExtraNpcInfo extra = (NpcInfo.ExtraNpcInfo) value;

                for (NpcInfo info : ((NpcRow) row).getInfos())
                    info.extra.copy(extra);
            } else {
                for (NpcInfo info : ((NpcRow) row).getInfos())
                    ReflectionUtils.set(field, info, value);
            }

            ConfigEntity.changed();
        }

        public static class NpcRow extends GenericTableModel.Row {

            public NpcRow(String name, NpcInfo data) {
                super(name, Collections.singleton(data));
            }

            @Override
            public Row update(Object data) {
                NpcInfo info = (NpcInfo) data;

                Collection<NpcInfo> infos = getInfos();
                if (infos.contains(info)) return this;

                if (infos instanceof Set) this.data = infos = new ArrayList<>(infos);
                info.copyOf(getInfo());
                infos.add((NpcInfo) data);
                return this;
            }

            @SuppressWarnings("unchecked")
            public Collection<NpcInfo> getInfos() {
                return (Collection<NpcInfo>) this.data;
            }

            public NpcInfo getInfo() {
                return getInfos().iterator().next();
            }
        }
    }

    private static class NpcMapFilter extends RowFilter<NpcTableModel, Integer> {

        private final Set<Integer> selected;

        public NpcMapFilter(Set<Integer> selected) {
            this.selected = selected;
        }

        @Override
        public boolean include(Entry<? extends NpcTableModel, ? extends Integer> entry) {
            if (selected.isEmpty()) return true;
            NpcTableModel.NpcRow row = (NpcTableModel.NpcRow) entry.getModel().getRow(entry.getIdentifier());
            return row.getInfos().stream().anyMatch(npc -> !Collections.disjoint(npc.mapList, selected));
        }
    }

    private static class JMapPicker extends JComboBox<String> {
        private static final String DEFAULT_ALL = "*";

        private final Set<Integer> selectedMaps = new HashSet<>();

        public JMapPicker(Consumer<Set<Integer>> onChange) {
            addItem(DEFAULT_ALL);
            setSelectedItem(DEFAULT_ALL);

            addActionListener(e -> {
                selectedMaps.clear();
                String selected = (String) getSelectedItem();
                if (selected == null || selected.equals(DEFAULT_ALL)) return;

                StarManager.getAllMaps().stream()
                        .filter(m -> m.id >= 0 && selected.equals(simplifyName(m.name)))
                        .mapToInt(m -> m.id)
                        .forEach(selectedMaps::add);

                onChange.accept(selectedMaps);
            });
        }

        public Set<Integer> getSelected() {
            return selectedMaps;
        }

        private void update(Collection<NpcInfo> npcInfos) {
            String map = (String) getSelectedItem();
            removeAllItems();
            addItem(DEFAULT_ALL);

            Set<Integer> maps = npcInfos.stream().flatMap(n -> n.mapList.stream()).collect(Collectors.toSet());
            StarManager.getAllMaps().stream()
                    .filter(m -> m.id >= 0 && maps.contains(m.id))
                    .map(m -> simplifyName(m.name))
                    .distinct()
                    .forEach(this::addItem);

            setSelectedItem(map);
        }

    }

}
