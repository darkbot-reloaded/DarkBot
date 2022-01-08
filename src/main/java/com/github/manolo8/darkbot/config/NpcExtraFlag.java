package com.github.manolo8.darkbot.config;

public interface NpcExtraFlag {
    String getName();
    String getShortName();
    String getDescription();
    default String getKey() {
        return null;
    }

    default String getId() {
        return getClass().isEnum() ? NpcInfo.getId((Enum<?>) this) : getClass().getCanonicalName() + getName();
    }
}
