package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

class FeatureDisplay extends JPanel {

    private final JCheckBox checkBox = new JCheckBox();

    FeatureDisplay(FeatureDefinition feature) {
        super(new MigLayout("ins 0, gap 0"));
        setOpaque(false);
        checkBox.setSelected(feature.isEnabled());

        checkBox.setText(feature.getName());
        if (!feature.getDescription().isEmpty())
            checkBox.setToolTipText(feature.getDescription());
        checkBox.setOpaque(false);
        checkBox.addActionListener(a -> feature.setStatus(checkBox.isSelected()));

        add(checkBox);
        add(new IssueList(feature.getIssues(), true), "hidemode 2");

        updateIssues(feature.getIssues());
        feature.getIssues().addListener(this::updateIssues);
    }

    private void updateIssues(IssueHandler issueList) {
        checkBox.setEnabled(issueList.canLoad());
        checkBox.setSelected(checkBox.isSelected() && issueList.canLoad());
    }

}
