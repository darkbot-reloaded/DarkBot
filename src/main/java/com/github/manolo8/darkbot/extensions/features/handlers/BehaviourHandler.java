package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.behaviours.AntiPush;
import com.github.manolo8.darkbot.core.itf.Behaviour;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BehaviourHandler extends FeatureHandler<Behaviour> {

    private static final Class[] NATIVE = new Class[]{AntiPush.class};

    private final Main main;
    private final FeatureRegistry featureRegistry;

    public BehaviourHandler(Main main, FeatureRegistry featureRegistry) {
        this.main = main;
        this.featureRegistry = featureRegistry;
    }

    @Override
    public Class[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<Behaviour>> tasks) {
        main.setBehaviours(tasks
                .map(featureRegistry::getFeature)
                .map(o -> o.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

}
