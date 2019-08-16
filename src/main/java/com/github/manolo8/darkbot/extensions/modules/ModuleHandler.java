package com.github.manolo8.darkbot.extensions.modules;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.extensions.features.FeatureRegistry;
import com.github.manolo8.darkbot.extensions.plugins.PluginListener;

import java.util.stream.Collectors;

public class ModuleHandler implements PluginListener {

    private FeatureRegistry featureRegistry;

    public ModuleHandler(FeatureRegistry featureRegistry) {
        this.featureRegistry = featureRegistry;
    }

    @Override
    public void afterLoadComplete() {
        ModuleSupplier.updateModules(featureRegistry.getFeatures(Module.class)
                .stream()
                .collect(Collectors.toMap(FeatureDefinition::getId, FeatureDefinition::getFeature)));
    }

}
