package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginIssue;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class IssueList extends JPanel {

    private IssueHandler issues;

    public IssueList(IssueHandler issues) {
        super(new MigLayout(issues.hasIssues() ? "wrap 1" : "ins 0", "[right]", "[top]"));
        this.issues = issues;
        setOpaque(false);
        setupUI();
    }

    private void setupUI() {
        removeAll();
        this.issues.getIssues().stream().map(this::getError).forEachOrdered(this::add);
    }

    private JLabel getError(PluginIssue pluginIssue) {
        JLabel label = new JLabel(pluginIssue.getMessage());
        label.setToolTipText(pluginIssue.getDescription());
        return label;
    }

}
