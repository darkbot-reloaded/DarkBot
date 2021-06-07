package eu.darkbot.api.items;

import eu.darkbot.api.managers.HeroItemsAPI;

import java.util.Locale;

/**
 * Represents all available categories of {@link HeroItemsAPI} items.
 */
public enum ItemCategory {
    LASERS,
    ROCKETS,
    ROCKET_LAUNCHERS,
    SPECIAL_ITEMS,
    MINES,
    CPUS,
    BUY_NOW,
    TECH_ITEMS,
    SHIP_ABILITIES,
    DRONE_FORMATIONS,
    PET;

    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
