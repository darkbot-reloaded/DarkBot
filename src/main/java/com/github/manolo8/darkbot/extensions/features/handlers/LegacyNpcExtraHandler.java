package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.NpcExtra;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LegacyNpcExtraHandler extends FeatureHandler<NpcExtraProvider> {

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
                .map(Optional::get)
                .map(NpcExtraProvider::values)
                .flatMap(Arrays::stream), true);
    }
}
