package com.github.manolo8.darkbot.gui.plugins;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.InstructionProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FeatureConfigButton extends MainButton {

    private final Main main;
    private final FeatureDefinition<Configurable<?>> feature;

    FeatureConfigButton(Main main, FeatureDefinition<Configurable<?>> feature) {
        super(UIUtils.getIcon("config"));
        setDisabledIcon(UIUtils.getIcon("config_unloaded"));
        this.main = main;
        this.feature = feature;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object paneMessage = new AdvancedConfig(main.pluginAPI, feature.getConfig());

        JComponent instructions = getInstructions();
        if (instructions != null) paneMessage = new Object[]{instructions, paneMessage};

        Popups.of(feature.getName(), paneMessage)
                .options()
                .border(BorderFactory.createEmptyBorder(0, 0, -4, 0))
                .parent(this)
                .showSync();
    }

    private JComponent getInstructions() {
        Object instance = feature.getInstance();
        if (instance instanceof InstructionProvider) {
            return ((InstructionProvider) instance).beforeConfig();
        }
        return null;
    }

}
