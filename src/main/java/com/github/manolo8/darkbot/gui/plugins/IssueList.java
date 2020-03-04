package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

class IssueList extends JPanel {

    IssueList(IssueHandler issues, boolean inline) {
        super(new MigLayout((inline ? "ins 0, gapx 5px, " : "") + "wrap 1", "[right]", "[top]"));

        setOpaque(false);
        setupUI(issues);
        issues.addListener(this::setupUI);
    }

    private void setupUI(IssueHandler issues) {
        removeAll();
        issues.getIssues().stream().map(this::getError).forEachOrdered(this::add);
        setVisible(!issues.getIssues().isEmpty());
    }

    private JLabel getError(PluginIssue pluginIssue) {
        JLabel label = new JLabel(pluginIssue.getMessage());
        if (pluginIssue.preventsLoading()) {
            label.setForeground(UIUtils.RED.brighter().brighter());
            Font baseFont = label.getFont();
            label.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD));
        }
        label.setToolTipText(pluginIssue.getDescription());
        label.addMouseListener(new SimpleMouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                ToolTipManager ttm = ToolTipManager.sharedInstance();
                int oldDelay = ttm.getInitialDelay();
                ttm.setInitialDelay(0);
                ttm.mouseMoved(new MouseEvent(label, 0, 0, 0, e.getX(), e.getY(), 0, false));
                SwingUtilities.invokeLater(() -> ttm.setInitialDelay(oldDelay));
            }
        });
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }

}
