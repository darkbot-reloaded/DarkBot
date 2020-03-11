package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.core.itf.InstructionProvider;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.modules.TemporalModule;

public class InstructionProviderDecorator extends FeatureDecorator<InstructionProvider> {

    @Override
    protected void load(FeatureDefinition<InstructionProvider> fd, InstructionProvider obj) {
        if (obj instanceof Module && !(obj instanceof TemporalModule)) obj.showInstructions(fd.getName());
    }

    @Override
    protected void unload(InstructionProvider obj) {
    }

}
