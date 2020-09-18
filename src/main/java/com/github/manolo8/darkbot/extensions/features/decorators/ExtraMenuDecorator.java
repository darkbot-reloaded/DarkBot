package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.titlebar.ExtraButton;

public class ExtraMenuDecorator extends FeatureDecorator<ExtraMenuProvider> {

    @Override
    protected void load(FeatureDefinition<ExtraMenuProvider> fd, ExtraMenuProvider obj) {
        ExtraButton.register(obj);
    }

    @Override
    protected void unload(ExtraMenuProvider obj) {
        ExtraButton.unregister(obj);
    }
}
