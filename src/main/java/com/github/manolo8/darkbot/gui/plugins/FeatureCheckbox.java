package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;

import javax.swing.*;

class FeatureCheckbox extends JCheckBox {

    private final boolean builtin;

    FeatureCheckbox(FeatureDefinition<?> feature) {
        super(feature.getName(), null, feature.canLoad());
        this.builtin = feature.getPlugin() == null;

        if (!feature.getDescription().isEmpty())
            setToolTipText(feature.getDescription());
        setOpaque(false);
        addActionListener(a -> feature.setStatus(isSelected()));

        updateIssues(feature.getIssues());
        feature.getIssues().addUIListener(this::updateIssues);
    }

    private void updateIssues(IssueHandler issueList) {
        setEnabled(!builtin && issueList.canLoad());
        setSelected(builtin || (isSelected() && issueList.canLoad()));
    }

}
