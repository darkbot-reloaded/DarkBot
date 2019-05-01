package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.JCheckBoxMenuItemNoClose;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JNpcInfoTable extends InfoTable implements OptionEditor {

    private int filteredMap = -1;

    public JNpcInfoTable(Config.Loot config) {
        super(new NpcTableModel(config.NPC_INFOS, config.ADDED_NPC));
        getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(3, SortOrder.DESCENDING),
                new RowSorter.SortKey(0, SortOrder.DESCENDING)));

        setDefaultEditor(ExtraNpcInfo.class, new ExtraNpcInfoEditor());

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
        private static final Class[] TYPES = new Class[]{String.class, Double.class, Integer.class, Boolean.class, Character.class, ExtraNpcInfo.class};

        private Map<String, NpcInfo> NPC_INFOS;

        NpcTableModel(Map<String, NpcInfo> NPC_INFOS, Lazy<String> added) {
            super(new Object[]{"Name", "Radius", "Priority", "Kill", "Ammo Key", "Extra"}, 0);
            (this.NPC_INFOS = NPC_INFOS).forEach(this::addEntry);
            added.add(n -> addEntry(n, NPC_INFOS.get(n)));
        }

        private void addEntry(String name, NpcInfo info) {
            addRow(new Object[]{name, info.radius, info.priority, info.kill, info.attackKey, new ExtraNpcInfo(info)});
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
            NpcInfo info = NPC_INFOS.get((String) this.getValueAt(row, 0));
            if (column == 1) info.radius = (Double) value;
            else if (column == 2) info.priority = (Integer) value;
            else if (column == 3) info.kill = (Boolean) value;
            else if (column == 4) info.attackKey = (Character) value;

            ConfigEntity.changed();
        }
    }

    private static class ExtraNpcInfo {
        NpcInfo info;

        ExtraNpcInfo(NpcInfo npcInfo) {
            this.info = npcInfo;
        }

        @Override
        public String toString() {
            return Stream.of(
                    info.noCircle ? "NC" : null,
                    info.ignoreOwnership ? "IO" : null,
                    info.ignoreAttacked ? "IA" : null,
                    info.passive ? "P" : null
            ).filter(Objects::nonNull).collect(Collectors.joining(","));
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

    public static class ExtraNpcInfoEditor extends AbstractCellEditor implements TableCellEditor {

        private ExtraNpcInfo curr;
        private NpcInfo info;
        private JLabel button = new JLabel();

        private JCheckBoxMenuItemNoClose
                noCircle = new JCheckBoxMenuItemNoClose("No circle", update(s -> info.noCircle = s)),
                ignoreOwnership = new JCheckBoxMenuItemNoClose("Ignore ownership", update(s -> info.ignoreOwnership = s)),
                ignoreAttacked = new JCheckBoxMenuItemNoClose("Ignore attacked", update(s -> info.ignoreAttacked = s)),
                passive = new JCheckBoxMenuItemNoClose("Passive", update(s -> info.passive = s));

        private JPopupMenu extraOptions = new JPopupMenu("Extra options");

        private Consumer<Boolean> update(Consumer<Boolean> bool) {
            return bool.andThen(s -> button.setText(curr.toString()));
        }

        ExtraNpcInfoEditor() {
            button.setOpaque(false);
            button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

            extraOptions.add(noCircle);
            extraOptions.add(ignoreOwnership);
            extraOptions.add(ignoreAttacked);
            extraOptions.add(passive);

            noCircle.setToolTipText("Don't circle the npc, just stay inside the radius");
            ignoreOwnership.setToolTipText("Continue killing the npc even if it has a white lock");
            ignoreAttacked.setToolTipText("Select the npc even if other players are already shooting it");
            passive.setToolTipText("Be passive towards this npc, only shoot if npc is shooting you");

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    extraOptions.show(button, e.getX(), e.getY());
                }
            });
            extraOptions.addPopupMenuListener(new PopupMenuListenerAdapter() {
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    stopCellEditing();
                }
            });
        }

        public Object getCellEditorValue() {
            return curr;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            curr = (ExtraNpcInfo) value;
            info = curr.info;
            button.setText(curr.toString());

            noCircle.setSelected(info.noCircle);
            ignoreOwnership.setSelected(info.ignoreOwnership);
            ignoreAttacked.setSelected(info.ignoreAttacked);
            passive.setSelected(info.passive);

            return button;
        }

    }

}
