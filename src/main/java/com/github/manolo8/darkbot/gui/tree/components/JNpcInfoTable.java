package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.GenericTableModel;
import com.github.manolo8.darkbot.gui.utils.MultiTableRowSorter;
import com.github.manolo8.darkbot.gui.utils.Strings;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.table.ExtraNpcInfoEditor;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JNpcInfoTable extends InfoTable<JNpcInfoTable.NpcTableModel, NpcInfo> implements OptionEditor {

    private JMapPicker mapPicker;
    private NpcMapFilter mapFilter;
    private Config.Loot config;

    public JNpcInfoTable(Config.Loot config) {
        super(new NpcTableModel(config), config.NPC_INFOS, config.MODIFIED_NPC, NpcInfo::new);
        this.config = config;

        mapPicker = new JMapPicker(maps -> getRowSorter().allRowsChanged());
        mapFilter = new NpcMapFilter(mapPicker.getSelected());

        @SuppressWarnings("unchecked")
        MultiTableRowSorter<JNpcInfoTable.NpcTableModel> sorter =
                (MultiTableRowSorter<JNpcInfoTable.NpcTableModel>) getRowSorter();
        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING),
                new RowSorter.SortKey(2, SortOrder.ASCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        sorter.putRowFilter("map", mapFilter);

        setDefaultEditor(NpcInfo.ExtraNpcInfo.class, new ExtraNpcInfoEditor());

        config.MODIFIED_NPC.add(s -> mapPicker.update(config.NPC_INFOS.values()));
        mapPicker.update(config.NPC_INFOS.values());

        getComponent().add(mapPicker, "grow, cell 1 0");
    }

    @Override
    protected MainButton removeButton() {
        return new NpcRemoveButton();
    }

    @Override
    public JComponent getComponent() {
        ((NpcTableModel) getModel()).refreshTable();
        return super.getComponent();
    }

    protected static class NpcTableModel extends GenericTableModel<NpcInfo> {

        private final Config.Loot config;
        private boolean grouped;

        NpcTableModel(Config.Loot config) {
            super(NpcInfo.class, config.NPC_INFOS, config.MODIFIED_NPC);
            this.config = config;
            this.grouped = config.GROUP_NPCS;
            rebuildTable();
            config.MODIFIED_NPC.add(n -> updateEntry(n, config.NPC_INFOS.get(n), true));
        }

        public void refreshTable() {
            if (grouped != (grouped = config.GROUP_NPCS)) rebuildTable();
        }

        @Override
        protected Row<NpcInfo> createRow(String name, NpcInfo data) {
            return new NpcRow(name, data);
        }

        @Override
        public String toTableName(String name) {
            return config != null && config.GROUP_NPCS ? Strings.simplifyName(name) : name;
        }

        @Override
        protected Object getValue(GenericTableModel.Row<NpcInfo> row, GenericTableModel.Column column) {
            if (column.field == null) return row.name;

            return ReflectionUtils.get(column.field, row.data);
        }

        @Override
        protected void setValue(Row<NpcInfo> row, Column column, Object value) {
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

        public static class NpcRow extends GenericTableModel.Row<NpcInfo> {

            private Collection<NpcInfo> infos;

            public NpcRow(String name, NpcInfo data) {
                super(name, data);
                this.infos = Collections.singleton(data);
            }

            @Override
            public NpcRow update(NpcInfo info) {
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
                if (selected == null) return;
                if (!selected.equals(DEFAULT_ALL))
                    StarManager.getAllMaps().stream()
                            .filter(m -> m.id >= 0 && selected.equals(Strings.simplifyName(m.name)))
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
                    .map(m -> Strings.simplifyName(m.name))
                    .distinct()
                    .forEach(this::addItem);

            setSelectedItem(map);
        }

    }

    private class NpcRemoveButton extends MainButton {
        NpcRemoveButton() {
            super(UIUtils.getIcon("remove"));
            super.actionColor = UIUtils.RED;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> toRemove = resolveNames(Arrays.stream(JNpcInfoTable.super.getSelectedRows())
                    .mapToObj(row -> (String) getValueAt(row, 0)))
                    .collect(Collectors.toList());

            toRemove.forEach(config.NPC_INFOS::remove);
            toRemove.forEach(config.MODIFIED_NPC::send);
        }

        private Stream<String> resolveNames(Stream<String> names) {
            if (!config.GROUP_NPCS) return names;
            Set<String> groupNames = names.collect(Collectors.toSet());

            return config.NPC_INFOS.keySet().stream()
                    .filter(name -> groupNames.contains(Strings.simplifyName(name)));
        }
    }

}
