package com.github.manolo8.darkbot.extensions.features.handlers;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleHandler extends FeatureHandler<Module> {

    private static final Class<?>[] NATIVE = new Class[]{CollectorModule.class, LootModule.class, LootNCollectorModule.class};

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
