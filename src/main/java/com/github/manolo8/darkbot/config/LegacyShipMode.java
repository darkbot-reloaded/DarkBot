package com.github.manolo8.darkbot.config;

import eu.darkbot.api.config.types.ShipMode;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface LegacyShipMode extends ShipMode {

    @Nullable
    Character getLegacyFormation();

    default boolean isLegacyFormation() {
        return true;
    }
}
