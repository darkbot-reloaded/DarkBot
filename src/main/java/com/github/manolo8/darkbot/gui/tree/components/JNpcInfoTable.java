package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;

public class JNpcInfoTable extends JTable implements OptionEditor {

    private static final String[] HEADERS = new String[]{"Name", "Radius", "Priority", "Kill", "No circle", "Ammo Key"};
    private static final Class[] TYPES = new Class[]{String.class, Double.class, Integer.class, Boolean.class, Boolean.class, Character.class};

    private static final CharacterEditor CHAR_EDITOR = new CharacterEditor();

    private JScrollPane scrollPane;
    private Map<String, NpcInfo> npcInfos;

    public JNpcInfoTable() {
        putClientProperty("ConfigTree", true);
        setDefaultEditor(Character.class, CHAR_EDITOR);

        setAutoCreateRowSorter(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);

        scrollPane = new JScrollPane(this) {
            public void setPreferredSize(Dimension preferredSize) {
                super.setPreferredSize(new Dimension(500, Math.min(250, 23 + (JNpcInfoTable.this.getRowCount() * 16))));
            }
        };
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public void edit(ConfigField field) {
        Map<String, NpcInfo> npcInfos = field.get();
        edit(npcInfos);
    }

    public void edit(Map<String, NpcInfo> npcInfos) {
        this.npcInfos = null;

        Object[][] content = npcInfos.entrySet().stream().map(e -> {
            NpcInfo info = e.getValue();
            return new Object[]{e.getKey(), info.radius, info.priority, info.kill, info.noCircle, info.attackKey};
        }).sorted(Comparator.comparing(objs -> (String) objs[0])).toArray(Object[][]::new);

        setModel(new DefaultTableModel(content, HEADERS));
        getColumnModel().getColumn(0).setPreferredWidth(200);
        this.npcInfos = npcInfos;
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
        if (npcInfos == null) return;
        super.setValueAt(value, row, column);
        NpcInfo info = npcInfos.get((String) this.getValueAt(row, 0));
        if (column == 1) info.radius = (Double) value;
        else if (column == 2) info.priority = (Integer) value;
        else if (column == 3) info.kill = (Boolean) value;
        else if (column == 4) info.noCircle = (Boolean) value;
        else if (column == 5) info.attackKey = (Character) value;
    }

    private static class CharacterEditor extends DefaultCellEditor {
        CharacterEditor() {
            super(new JCharField());
            JCharField charField = (JCharField) getComponent();
            charField.removeActionListener(delegate);
            charField.addActionListener(delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    charField.setText(value == null ? "" : value.toString());
                }

                public Object getCellEditorValue() {
                    return charField.getValue();
                }
            });
        }
    }

}
