package com.github.manolo8.darkbot.config.types.suppliers;

import eu.darkbot.api.config.annotations.Configuration;

@Configuration("config.general.safety.revive_location.list")
public enum ReviveLocation {
    BASE, PORTAL, SPOT;

    public long getId() {
        return ordinal() + 1;
    }
}
