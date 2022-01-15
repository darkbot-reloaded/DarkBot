package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Configurable;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConfigurableDecorator extends FeatureDecorator<Configurable> {

    @Override
    protected void load(FeatureDefinition<Configurable> fd, Configurable obj) {
        ConfigSetting<?> config = fd.getConfig();
        if (config == null)
            throw new IllegalStateException("Configurable object has no config defined");

        obj.setConfig(config);
    }

    @Override
    protected void unload(Configurable obj) {
    }

}
