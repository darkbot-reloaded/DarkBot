package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.utils.I18n;

import java.util.Locale;

public enum NpcExtra implements NpcExtraFlag {
    NO_CIRCLE, IGNORE_OWNERSHIP, IGNORE_ATTACKED, PASSIVE, ATTACK_SECOND, USE_RSB, PET_LOCATOR;

    @Override
    public String getShortName() {
        return I18n.getOrDefault("config.loot.npc_table.extra." + name().toLowerCase(Locale.ROOT) + ".short", name());
    }

    @Override
    public String getName() {
        return I18n.getOrDefault("config.loot.npc_table.extra." + name().toLowerCase(Locale.ROOT), name());
    }

    @Override
    public String getDescription() {
        return I18n.getOrDefault("config.loot.npc_table.extra." + name().toLowerCase(Locale.ROOT) + ".desc", name());
    }

    @Feature(name = "Npc extra flag provider", description = "Provides default npc extra flags")
    public static class DefaultNpcExtraProvider implements NpcExtraProvider {
        @Override
        public NpcExtraFlag[] values() {
            return NpcExtra.values();
        }
    }

}