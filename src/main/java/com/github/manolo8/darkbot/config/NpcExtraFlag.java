package com.github.manolo8.darkbot.config;

import eu.darkbot.api.config.NpcInfo;

public interface NpcExtraFlag extends NpcInfo.ExtraFlag {
    String getName();
    String getShortName();
    String getDescription();
    default String getKey() {
        return null;
    }

    default String getId() {
        return getClass().getCanonicalName() + (getClass().isEnum() ? ((Enum<?>) this).name() : getName());
    }
}
