package com.github.manolo8.darkbot.config.types.suppliers;

import eu.darkbot.api.config.annotations.Configuration;

@Configuration("config.bot_settings.map_display.toggle")
public enum DisplayFlag {
    USERNAMES,
    NPC_NAMES,
    HERO_NAME,
    HP_SHIELD_NUM,
    ZONES,
    STATS_AREA,
    GROUP_AREA, GROUP_NAMES,
    BOOSTER_AREA, SORT_BOOSTERS,
    RESOURCE_NAMES,
    SHOW_PET,
    SHOW_DESTINATION
}
