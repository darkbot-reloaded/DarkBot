package com.github.manolo8.darkbot.extensions.modules;

import com.github.manolo8.darkbot.config.types.suppliers.ModuleSupplier;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.extensions.plugins.AbstractPluginFeatureHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.DummyModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

public class ModuleHandler extends AbstractPluginFeatureHandler<Module, CustomModule> {

    public ModuleHandler(PluginHandler pluginHandler) {
        super(pluginHandler);
    }

    @Override
    protected void registerDefaults() {
        registerFeature(CollectorModule.class);
        registerFeature(LootModule.class);
        registerFeature(LootNCollectorModule.class);
    }

    @Override
    protected String[] getFeatures(PluginDefinition plugin) {
        return plugin.modules;
    }

    @Override
    protected void registerFeature(Class<? extends Module> module) {
        CustomModule cm = module.getAnnotation(CustomModule.class);
        if (cm == null) throw new IllegalArgumentException("Can't load custom module not annotated with @CustomModule");
        register(module, cm);
    }

    @Override
    protected void afterRegistration() {
        ModuleSupplier.updateModules(FEATURES_BY_ID);
    }

    @Override
    protected Module getDefault() {
        return new DummyModule();
    }

}
