package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;

import javax.swing.*;

class FeatureCheckbox extends JCheckBox {

    FeatureCheckbox(FeatureDefinition<?> feature) {
        super(feature.getName(), null, feature.canLoad());

        if (!feature.getDescription().isEmpty())
            setToolTipText(feature.getDescription());
        setOpaque(false);
        addActionListener(a -> feature.setStatus(isSelected()));

        updateIssues(feature.getIssues());
        feature.getIssues().addListener(this::updateIssues);
    }

    private void updateIssues(IssueHandler issueList) {
        setEnabled(issueList.canLoad());
        setSelected(isSelected() && issueList.canLoad());
    }

}
