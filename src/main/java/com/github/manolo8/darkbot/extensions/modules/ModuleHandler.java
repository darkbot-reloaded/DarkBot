package com.github.manolo8.darkbot.extensions.modules;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.plugins.AbstractPluginFeatureHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.EventModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

public class ModuleHandler extends AbstractPluginFeatureHandler<Module> {

    public ModuleHandler(PluginHandler pluginHandler) {
        super(pluginHandler);
    }

    @Override
    protected void registerDefaults() {
        register("Collector", "Resource-only collector module. Can cloack.", CollectorModule.class);
        register("Npc Killer", "Npc-only module. Will never pick up resources.", LootModule.class);
        register("Kill & Collect", "Kills npcs and collects resources at the same time.", LootNCollectorModule.class);
        register("Experiment zones", "Used for the plutus dark orbit event", EventModule.class);
    }

    @Override
    protected String[] getFeatures(PluginDefinition plugin) {
        return plugin.modules;
    }

    @Override
    protected void registerFeature(Class<Module> module) {
        CustomModule cm = module.getAnnotation(CustomModule.class);
        if (cm == null) throw new IllegalArgumentException("Can't load custom module not annotated with @CustomModule");
        register(cm.name(), cm.description(), module);
    }

    @Override
    protected void afterRegistration() {
        ModuleSupplier.updateModules(FEATURE_NAMES_BY_ID, FEATURE_DESCRIPTIONS_BY_ID);
    }

    @Override
    protected Module getDefault() {
        return new LootNCollectorModule();
    }

}
