package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;

import java.util.Optional;
import java.util.stream.Stream;

// TODO: implement a separate handler for the new NpcExtra, as the logic is pretty different
public class NpcExtraHandler extends FeatureHandler<NpcExtraProvider> {

    private static final Class<?>[] NATIVE = new Class[]{NpcExtra.DefaultNpcExtraProvider.class};

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<NpcExtraProvider>> flags) {
        NpcInfo.setNpcFlags(flags
                .map(featureRegistry::getFeature)
                .filter(Optional::isPresent)
                .map(Optional::get));
    }
}
