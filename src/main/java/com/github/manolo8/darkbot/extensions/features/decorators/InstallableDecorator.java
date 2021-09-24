package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.Installable;

public class InstallableDecorator extends FeatureDecorator<Installable> {

    private final PluginAPI pluginAPI;

    public InstallableDecorator(PluginAPI pluginAPI) {
        this.pluginAPI = pluginAPI;
    }

    @Override
    protected void load(FeatureDefinition<Installable> fd, Installable obj) {
        obj.install(pluginAPI);
    }

    @Override
    protected void unload(Installable obj) {
        obj.uninstall();
    }

}
