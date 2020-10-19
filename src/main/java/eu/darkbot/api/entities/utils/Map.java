package eu.darkbot.api.entities.utils;

public class Map extends com.github.manolo8.darkbot.core.objects.Map {
    public Map(int id, String name, boolean pvp, boolean gg) {
        super(id, name, pvp, gg);
    }

    public Map(int id, String name, String shortName, boolean pvp, boolean gg) {
        super(id, name, shortName, pvp, gg);
    }
}
