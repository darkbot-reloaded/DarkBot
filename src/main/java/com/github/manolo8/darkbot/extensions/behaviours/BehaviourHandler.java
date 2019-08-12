package com.github.manolo8.darkbot.extensions.behaviours;

import com.github.manolo8.darkbot.extensions.plugins.AbstractPluginFeatureHandler;
import com.github.manolo8.darkbot.extensions.plugins.PluginDefinition;
import com.github.manolo8.darkbot.extensions.plugins.PluginHandler;

public class BehaviourHandler extends AbstractPluginFeatureHandler<Behaviour, CustomBehaviour> {

    public BehaviourHandler(PluginHandler pluginHandler) {
        super(pluginHandler);
    }

    @Override
    protected void registerDefaults() {}

    @Override
    protected String[] getFeatures(PluginDefinition plugin) {
        return plugin.behaviours;
    }

    @Override
    protected void registerFeature(Class<? extends Behaviour> behaviour) {
        CustomBehaviour cb = behaviour.getAnnotation(CustomBehaviour.class);
        if (cb == null) throw new IllegalArgumentException("Can't load behaviour not annotated with @CustomBehaviour");
        register(behaviour, cb);
    }

    @Override
    protected void afterRegistration() {
        System.out.println("Registered behaviours (not being used currently): " + FEATURES_BY_ID.toString());
    }

    @Override
    protected Behaviour getDefault() {
        return null;
    }
}
