package com.github.manolo8.darkbot.config;

public interface NpcExtraFlag {
    String getName();
    String getShortName();
    String getDescription();
    default String getKey() {
        return null;
    }

    default String getId() {
        return getClass().getCanonicalName() + (getClass().isEnum() ? ((Enum) this).name() : getName());
    }
}
