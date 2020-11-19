package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.config.NpcExtraFlag;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.gui.tree.components.JNpcInfoTable;
import com.github.manolo8.darkbot.gui.utils.JCheckBoxMenuItemNoClose;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtraNpcInfoEditor extends AbstractCellEditor implements TableCellEditor {

    private final javax.swing.JLabel button = new javax.swing.JLabel();

    private Map<String, JMenuFlag> options = new HashMap<>();
    private final JPopupMenu extraOptions = new JPopupMenu("Extra options");

    private NpcInfo.ExtraNpcInfo value;

    private int tooltipDelay = -1;

    public ExtraNpcInfoEditor() {
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isShowing()) {
                    extraOptions.show(button, e.getX(), e.getY());
                    if (tooltipDelay == -1) tooltipDelay = ToolTipManager.sharedInstance().getInitialDelay();
                    ToolTipManager.sharedInstance().setInitialDelay(0);
                }
            }
        });
        extraOptions.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                stopCellEditing();
                if (tooltipDelay != -1) ToolTipManager.sharedInstance().setInitialDelay(tooltipDelay);
            }
        });
    }

    private void updateMenuEntries() {
        if (NpcInfo.NPC_FLAGS.keySet().equals(options.keySet())) return;
        extraOptions.removeAll();
        options = NpcInfo.NPC_FLAGS.values().stream()
                .map(JMenuFlag::new)
                .peek(extraOptions::add)
                .collect(Collectors.toMap(fl -> fl.flag, Function.identity()));
    }

    public Object getCellEditorValue() {
        return value;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.value = (NpcInfo.ExtraNpcInfo) value;
        button.setText(value.toString());

        updateMenuEntries();

        options.values().forEach(option -> option.setSelected(this.value.has(option.flag)));
        return button;
    }

    private class JMenuFlag extends JCheckBoxMenuItemNoClose {

        private final String flag;

        JMenuFlag(NpcExtraFlag flag) {
            super(flag.getName());
            this.flag = flag.getId();
            if (flag.getDescription() != null) setToolTipText(flag.getDescription());
            addActionListener(a -> {
                value.set(this.flag, isSelected());
                button.setText(value.toString());
            });
        }
    }

}
