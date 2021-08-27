package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.config.ConfigSetting;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ConfigurableDecorator extends FeatureDecorator<Configurable> {

    @Override
    protected void load(FeatureDefinition<Configurable> fd, Configurable obj) {
        ConfigSetting<?> config = fd.getConfig();
        if (config == null)
            throw new IllegalStateException("Configurable object has no config defined");

        obj.setConfig(config.getValue());
    }

    @Override
    protected void unload(Configurable obj) {
    }

}
