package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FeatureConfigButton extends MainButton {

    private Config config;
    private FeatureDefinition<Behaviour> feature;

    FeatureConfigButton(Config config, FeatureDefinition<Behaviour> feature) {
        super(UIUtils.getIcon("config"));
        setToolTipText("Show configuration");
        this.config = config;
        this.feature = feature;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (feature.getInstance() == null) {
            Popups.showMessageAsync("Can't edit config", "Config not available for unloaded feature\n" +
                    "Enable the feature and load it, and try again.", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Popups.showMessageSync(feature.getName(),
                    new JOptionPane(new AdvancedConfig(this.config.CUSTOM_CONFIGS.get(feature.getId()))));
        }
    }

}
