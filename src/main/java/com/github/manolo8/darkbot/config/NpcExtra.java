package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.core.itf.NpcExtraProvider;
import com.github.manolo8.darkbot.extensions.features.Feature;

public enum NpcExtra implements NpcExtraFlag {
    NO_CIRCLE("NC", "No circle", "Don't circle the npc, just stay inside the radius"),
    IGNORE_OWNERSHIP("IO", "Ignore ownership", "Continue killing the npc even if it has a white lock"),
    IGNORE_ATTACKED("IA", "Ignore attacked", "Select the npc even if other players are already shooting it"),
    PASSIVE("P", "Passive", "Be passive towards this npc, only shoot if npc is shooting you"),
    ATTACK_SECOND("AS", "Attack second", "<html>Only shoot if others are attacking already.<br><strong>Must</strong> also select ignore attacked & ignore ownership</html>");

    private final String shortName, name, description;
    NpcExtra(String shortName, String name, String description) {
        this.shortName = shortName;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Feature(name = "Npc extra flag provider", description = "Provides default npc extra flags")
    public static class DefaultNpcExtraProvider implements NpcExtraProvider {
        @Override
        public NpcExtraFlag[] values() {
            return NpcExtra.values();
        }
    }

}