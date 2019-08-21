package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;

public class InstructionProviderDecorator extends FeatureDecorator<InstructionProvider> {

    @Override
    protected void load(FeatureDefinition<InstructionProvider> fd, InstructionProvider obj) {
        obj.showInstructions();
    }

    @Override
    protected void unload(InstructionProvider obj) {
    }

}
