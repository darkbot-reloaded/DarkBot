package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;

public class InstructionProviderDecorator extends FeatureDecorator<InstructionProvider> {

    @Override
    protected void load(FeatureDefinition<InstructionProvider> fd, InstructionProvider obj) {
        Popups.showMessageAsync("Instructions", obj.instructions(), JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void unload(InstructionProvider obj) {
    }

}
