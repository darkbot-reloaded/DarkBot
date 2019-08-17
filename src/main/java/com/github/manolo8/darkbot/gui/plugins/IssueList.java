package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

class IssueList extends JPanel {

    IssueList(IssueHandler issues, boolean inline) {
        super(new MigLayout((inline ? "ins 0," : "") + "wrap 1", "[right]", "[top]"));

        setOpaque(false);
        setBackground(Color.BLUE);
        setupUI(issues);
        issues.addListener(this::setupUI);
    }

    private void setupUI(IssueHandler issues) {
        removeAll();
        issues.getIssues().stream().map(this::getError).forEachOrdered(this::add);
        setVisible(issues.hasIssues());
    }

    private JLabel getError(PluginIssue pluginIssue) {
        JLabel label = new JLabel(pluginIssue.getMessage());
        label.setToolTipText(pluginIssue.getDescription());
        return label;
    }

}
