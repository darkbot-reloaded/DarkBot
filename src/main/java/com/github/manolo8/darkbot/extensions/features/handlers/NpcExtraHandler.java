package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;

import java.util.stream.Stream;

public class NpcExtraHandler extends FeatureHandler<NpcExtraProvider> {

    private static final Class[] NATIVE = new Class[]{NpcExtra.DefaultNpcExtraProvider.class};

    private final FeatureRegistry featureRegistry;

    public NpcExtraHandler(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    @Override
    public Class[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<NpcExtraProvider>> extras) {
        extras.forEach(featureRegistry::getFeature);
    }
}
