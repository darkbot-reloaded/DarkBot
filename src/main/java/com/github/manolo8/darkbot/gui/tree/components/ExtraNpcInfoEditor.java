package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.NpcExtraFlag;
import com.github.manolo8.darkbot.config.NpcInfo;
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

    private JNpcInfoTable.ExtraNpcInfoList curr;
    private Collection<NpcInfo> infos;
    private javax.swing.JLabel button = new javax.swing.JLabel();

    private Map<String, JMenuFlag> options = new HashMap<>();
    private JPopupMenu extraOptions = new JPopupMenu("Extra options");

    private int tooltipDelay = -1;

    ExtraNpcInfoEditor() {
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

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
        return curr;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        curr = (JNpcInfoTable.ExtraNpcInfoList) value;
        infos = curr.infos;
        button.setText(curr.toString());

        updateMenuEntries();

        NpcInfo info = infos.iterator().next();
        options.values().forEach(option -> option.setSelected(info.extra.has(option.flag)));
        return button;
    }

    private class JMenuFlag extends JCheckBoxMenuItemNoClose {

        private final String flag;

        JMenuFlag(NpcExtraFlag flag) {
            super(flag.getName());
            this.flag = flag.getId();
            if (flag.getDescription() != null) setToolTipText(flag.getDescription());
            addActionListener(a -> {
                infos.forEach(info -> info.extra.set(this.flag, isSelected()));
                button.setText(curr.toString());
            });
        }
    }

}
