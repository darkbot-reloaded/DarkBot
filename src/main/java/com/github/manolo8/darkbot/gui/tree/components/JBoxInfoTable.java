package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class JBoxInfoTable extends JTable implements OptionEditor {

    private static final String[] HEADERS = new String[]{"Name", "Collect", "Wait"};
    private static final Class[] TYPES = new Class[]{String.class, Boolean.class, Integer.class};

    private JScrollPane scrollPane;
    private Map<String, BoxInfo> boxInfos;

    public JBoxInfoTable() {
        putClientProperty("ConfigTree", true);
        setAutoCreateRowSorter(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        getTableHeader().setReorderingAllowed(false);

        scrollPane = new JScrollPane(this) {
            public void setPreferredSize(Dimension preferredSize) {
                super.setPreferredSize(new Dimension(330, Math.min(250, 23 + (JBoxInfoTable.this.getRowCount() * 16))));
            }
        };
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public void edit(ConfigField field) {
        Map<String, BoxInfo> boxInfos = field.get();
        edit(boxInfos);
    }

    public void edit(Map<String, BoxInfo> boxInfos) {
        this.boxInfos = null;
        Object[][] content = boxInfos.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue().collect, entry.getValue().waitTime})
                .toArray(Object[][]::new);

        setModel(new DefaultTableModel(content, HEADERS));
        getColumnModel().getColumn(0).setPreferredWidth(200);
        this.boxInfos = boxInfos;
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
        if (boxInfos == null) return;
        super.setValueAt(value, row, column);
        BoxInfo info = boxInfos.get((String) this.getValueAt(row, 0));
        if (column == 1) info.collect = (Boolean) value;
        else if (column == 2) info.waitTime = (Integer) value;
    }

}
