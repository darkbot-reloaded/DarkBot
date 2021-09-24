package com.github.manolo8.darkbot.core.itf;

import eu.darkbot.api.config.ConfigSetting;

public interface Configurable<T> extends eu.darkbot.api.extensions.Configurable<T> {
    void setConfig(T config);

    @Override
    default void setConfig(ConfigSetting<T> configSetting) {
        setConfig(configSetting.getValue());
    }
}
