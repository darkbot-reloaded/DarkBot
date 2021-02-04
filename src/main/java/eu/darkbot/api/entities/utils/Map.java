package eu.darkbot.api.entities.utils;

import org.jetbrains.annotations.Nullable;

public interface Map {
    /**
     * Gets id of current map.
     * <a href = http://www.darkorbit.bigpoint.com/spacemap/graphics/maps-config.xml>List of maps</a>
     *
     * @return id of the map
     */
    int getId();

    /**
     * @return name of the map.
     */
    String getName();

    /**
     * @return short name of the map.
     */
    @Nullable
    String getShortName();

    /**
     * Is map a PvP zone.
     * For example 4-x maps.
     */
    boolean isPvp();

    /**
     * @return true if map is galaxy gate type.
     * For example: Alpha, Zeta, Hades etc.
     */
    boolean isGg();
}
