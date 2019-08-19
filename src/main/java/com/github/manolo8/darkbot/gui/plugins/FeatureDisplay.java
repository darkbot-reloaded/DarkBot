package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.plugins.IssueHandler;
import com.github.manolo8.darkbot.gui.titlebar.ConfigButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

class FeatureDisplay extends JPanel {

    private final JCheckBox checkBox = new JCheckBox();

    FeatureDisplay(Main main, FeatureDefinition<?> feature) {
        super(new MigLayout("ins 0, gap 0"));
        setOpaque(false);
        checkBox.setSelected(feature.isEnabled());

        checkBox.setText(feature.getName());
        if (!feature.getDescription().isEmpty())
            checkBox.setToolTipText(feature.getDescription());
        checkBox.setOpaque(false);
        checkBox.addActionListener(a -> feature.setStatus(checkBox.isSelected()));

        add(checkBox);
        if (Configurable.class.isAssignableFrom(feature.getClazz())) {
            add(new FeatureConfigButton(main.config, (FeatureDefinition<Behaviour>) feature));
        }
        add(new IssueList(feature.getIssues(), true), "hidemode 2");

        updateIssues(feature.getIssues());
        feature.getIssues().addListener(this::updateIssues);
    }

    private void updateIssues(IssueHandler issueList) {
        checkBox.setEnabled(issueList.canLoad());
        checkBox.setSelected(checkBox.isSelected() && issueList.canLoad());
    }

}
