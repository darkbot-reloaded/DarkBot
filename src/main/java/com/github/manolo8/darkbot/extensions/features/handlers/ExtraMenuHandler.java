package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.core.itf.ExtraMenuProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.gui.titlebar.ExtraButton;

import java.util.Optional;
import java.util.stream.Stream;

public class ExtraMenuHandler extends FeatureHandler<ExtraMenuProvider> {

    private static final Class<?>[] NATIVE = new Class[]{ExtraButton.DefaultExtraMenuProvider.class};

    private final FeatureRegistry featureRegistry;

    public ExtraMenuHandler(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<ExtraMenuProvider>> extras) {
        ExtraButton.setExtraDecorations(extras
                .map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get));
    }
}
