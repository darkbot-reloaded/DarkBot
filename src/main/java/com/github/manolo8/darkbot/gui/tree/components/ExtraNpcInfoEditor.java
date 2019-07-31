package com.github.manolo8.darkbot.gui.tree.components;

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
import java.util.function.Consumer;

public class ExtraNpcInfoEditor extends AbstractCellEditor implements TableCellEditor {

    private JNpcInfoTable.ExtraNpcInfoList curr;
    private Collection<NpcInfo> infos;
    private JLabel button = new JLabel();

    private JCheckBoxMenuItemNoClose
            noCircle = new JCheckBoxMenuItemNoClose("No circle", update(s -> infos.forEach(info -> info.extra.noCircle = s))),
            ignoreOwnership = new JCheckBoxMenuItemNoClose("Ignore ownership", update(s -> infos.forEach(info -> info.extra.ignoreOwnership = s))),
            ignoreAttacked = new JCheckBoxMenuItemNoClose("Ignore attacked", update(s -> infos.forEach(info -> info.extra.ignoreAttacked = s))),
            passive = new JCheckBoxMenuItemNoClose("Passive", update(s -> infos.forEach(info -> info.extra.passive = s))),
            attackSecond = new JCheckBoxMenuItemNoClose("Attack second", update(s -> infos.forEach(info -> info.extra.attackSecond = s)));

    private JPopupMenu extraOptions = new JPopupMenu("Extra options");

    private Consumer<Boolean> update(Consumer<Boolean> bool) {
        return bool.andThen(s -> button.setText(curr.toString()));
    }
    private int tooltipDelay = -1;

    ExtraNpcInfoEditor() {
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        extraOptions.add(noCircle);
        extraOptions.add(ignoreOwnership);
        extraOptions.add(ignoreAttacked);
        extraOptions.add(passive);
        extraOptions.add(attackSecond);

        noCircle.setToolTipText("Don't circle the npc, just stay inside the radius");
        ignoreOwnership.setToolTipText("Continue killing the npc even if it has a white lock");
        ignoreAttacked.setToolTipText("Select the npc even if other players are already shooting it");
        passive.setToolTipText("Be passive towards this npc, only shoot if npc is shooting you");
        attackSecond.setToolTipText("<html>Only shoot if others are attacking already.<br><strong>Must</strong> also select ignore attacked & ignore ownership</html>");

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

    public Object getCellEditorValue() {
        return curr;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        curr = (JNpcInfoTable.ExtraNpcInfoList) value;
        infos = curr.infos;
        button.setText(curr.toString());

        NpcInfo info = infos.iterator().next();

        noCircle.setSelected(info.extra.noCircle);
        ignoreOwnership.setSelected(info.extra.ignoreOwnership);
        ignoreAttacked.setSelected(info.extra.ignoreAttacked);
        passive.setSelected(info.extra.passive);
        attackSecond.setSelected(info.extra.attackSecond);

        return button;
    }

}
