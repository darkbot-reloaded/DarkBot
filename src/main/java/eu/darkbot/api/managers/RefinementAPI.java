package eu.darkbot.api.managers;

import eu.darkbot.api.objects.Gui;
import eu.darkbot.api.API;
import org.jetbrains.annotations.Nullable;

/**
 * API for refinement window
 */
public interface RefinementAPI extends Gui, API {

    /**
     * gets the specific {@code type} of ore
     *
     * @param type  the {@code OreType} you want to get
     * @return the specified {@code type} else null
     * @see Ore
     * @see OreType
     */
    @Nullable
    Ore get(OreType type);

    /**
     * Specific information about ores
     */
    class Ore {
        private String name, fuzzyName;
        private int amount;

        public String getName() {
            return name;
        }

        public String getFuzzyName() {
            return fuzzyName;
        }

        public int getAmount() {
            return amount;
        }
    }

    /**
     * Types of Ores visible in refinery window
     */
    enum OreType {
        PROMETIUM,
        ENDURIUM,
        TERBIUM,
        XENOMIT,
        PALLADIUM,
        PROMETID,
        DURANIUM,
        PROMERIUM,
        SEPROM,
        OSMIUM
    }
}
