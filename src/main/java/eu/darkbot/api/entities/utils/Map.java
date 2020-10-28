package eu.darkbot.api.entities.utils;

public class Map extends com.github.manolo8.darkbot.core.objects.Map {

    public Map(int id, String name, boolean pvp, boolean gg) {
        super(id, name, pvp, gg);
    }

    public Map(int id, String name, String shortName, boolean pvp, boolean gg) {
        super(id, name, shortName, pvp, gg);
    }

    /**
     * Gets id of current map.
     * <a href = http://www.darkorbit.bigpoint.com/spacemap/graphics/maps-config.xml>List of maps</a>
     *
     * @return id of the map
     */
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * Is map a PvP zone.
     * For example 4-x maps.
     */
    public boolean isPvp() {
        return pvp;
    }

    /**
     * @return true if map is galaxy gate type.
     * For example: Alpha, Zeta, Hades etc.
     */
    public boolean isGg() {
        return gg;
    }
}
