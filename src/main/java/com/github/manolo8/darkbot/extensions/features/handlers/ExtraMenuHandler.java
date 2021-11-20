package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.gui.titlebar.ExtraButton;
import eu.darkbot.api.extensions.ExtraMenus;

import java.util.Optional;
import java.util.stream.Stream;

public class ExtraMenuHandler extends FeatureHandler<ExtraMenus> {

    private static final Class<?>[] NATIVE = new Class[] {
            ExtraButton.DefaultExtraMenuProvider.class,
            ExtraButton.PluginExtraMenuProvider.class
    };

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<ExtraMenus>> extras) {
        ExtraButton.setExtraDecorations(extras
                .map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get), featureRegistry);
    }
}
