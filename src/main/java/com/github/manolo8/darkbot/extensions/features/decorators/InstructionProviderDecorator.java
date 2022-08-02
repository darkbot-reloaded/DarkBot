package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.modules.TemporalModule;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.extensions.InstructionProvider;
import eu.darkbot.api.extensions.Module;

import javax.swing.*;

public class InstructionProviderDecorator extends FeatureDecorator<InstructionProvider> {

    @Override
    protected void load(FeatureDefinition<InstructionProvider> fd, InstructionProvider obj) {
        if (obj instanceof Module && !(obj instanceof TemporalModule)) showInstructions(obj, fd.getName());
    }

    @Override
    protected void unload(InstructionProvider obj) {
    }

    public static void showInstructions(Object featureInstance, String name) {
        // Legacy instruction provider, respect the custom showInstructions method
        if (featureInstance instanceof com.github.manolo8.darkbot.core.itf.InstructionProvider) {
            ((com.github.manolo8.darkbot.core.itf.InstructionProvider) featureInstance).showInstructions(name);
            return;
        }

        // New instruction provider
        if (featureInstance instanceof InstructionProvider) {
            InstructionProvider ip = (InstructionProvider) featureInstance;

            Object display = ip.instructionsComponent();
            if (display == null) display = ip.instructions();
            if (display == null) return; // If both text & component are null, do nothing
            Popups.of(I18n.get("module.instructions.title") + " - " + name, display, JOptionPane.INFORMATION_MESSAGE)
                    .showAsync();
        }
    }

}
