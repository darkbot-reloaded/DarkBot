package com.github.manolo8.darkbot.gui.utils.table;

import com.github.manolo8.darkbot.config.NpcExtraFlag;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.gui.utils.JCheckBoxMenuItemNoClose;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExtraNpcInfoEditor extends TableDelegateEditor<JLabel> {

    private static final ToolTipManager TOOLTIPS = ToolTipManager.sharedInstance();

    private Map<String, JMenuFlag> options = new HashMap<>();
    private final JPopupMenu extraOptions = new JPopupMenu("Extra options");

    private final NpcInfo.ExtraNpcInfo value = new NpcInfo.ExtraNpcInfo();

    private int tooltipDelay = -1;

    public ExtraNpcInfoEditor() {
        super(new JLabel());
        extraOptions.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (tooltipDelay != -1) TOOLTIPS.setInitialDelay(tooltipDelay);
            }
        });
    }

    public Object getValue() {
        return value;
    }

    protected void setValue(Object val) {
        NpcInfo.ExtraNpcInfo value = (NpcInfo.ExtraNpcInfo) val;

        this.delegate.setText(value.toString());
        this.value.copy(value);

        if (!NpcInfo.NPC_FLAGS.keySet().equals(options.keySet())) {
            extraOptions.removeAll();
            options = NpcInfo.NPC_FLAGS.values().stream()
                    .map(JMenuFlag::new)
                    .peek(extraOptions::add)
                    .collect(Collectors.toMap(fl -> fl.flag, Function.identity()));
            extraOptions.pack();
        }
        options.values().forEach(option -> option.setSelected(this.value.has(option.flag)));
    }

    public void startEditing(JTable table, Object value, boolean isSelected, int row, int column) {
        SwingUtilities.invokeLater(() -> {
            if (!table.isShowing()) return;

            Rectangle r = table.getCellRect(row, column, false);
            int width = extraOptions.getWidth();
            extraOptions.show(table, (int) r.getX() - width, (int) r.getY());

            if (width != (width = extraOptions.getWidth()))
                extraOptions.show(table, (int) r.getX() - width, (int) r.getY());

            if (tooltipDelay == -1) tooltipDelay = TOOLTIPS.getInitialDelay();
            TOOLTIPS.setInitialDelay(0);
        });
    }

    private class JMenuFlag extends JCheckBoxMenuItemNoClose {

        private final String flag;

        JMenuFlag(NpcExtraFlag flag) {
            super(flag.getName());
            this.flag = flag.getId();
            if (flag.getDescription() != null) setToolTipText(flag.getDescription());
            addActionListener(a -> {
                value.set(this.flag, isSelected());
                delegate.setText(value.toString());
            });
        }
    }

}
