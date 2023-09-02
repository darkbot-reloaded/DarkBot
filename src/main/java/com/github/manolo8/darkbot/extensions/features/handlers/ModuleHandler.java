package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.shared.modules.CollectorModule;
import eu.darkbot.shared.modules.LootCollectorModule;
import eu.darkbot.shared.modules.LootModule;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleHandler extends FeatureHandler<eu.darkbot.api.extensions.Module> {

    private static final Class<?>[] NATIVE = new Class[]{
            CollectorModule.class, LootModule.class, LootCollectorModule.class};

    @Override
    public Class<?>[] getNativeFeatures() {
        return NATIVE;
    }

    @Override
    public void update(Stream<FeatureDefinition<Module>> modules) {
        ModuleSupplier.updateModules(modules.collect(
                Collectors.toMap(FeatureDefinition::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new)));
    }

}
