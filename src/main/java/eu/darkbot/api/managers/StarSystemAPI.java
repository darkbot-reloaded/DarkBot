package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.utils.Listener;
import eu.darkbot.utils.ArrayUtils;

import java.util.Collection;
import java.util.List;

/**
 * API to add, manage, retrieve maps.
 */
public interface StarSystemAPI extends API {
    List<String> HOME_MAPS         = ArrayUtils.asImmutableList("1-1", "2-1", "3-1");
    List<String> OUTPOST_HOME_MAPS = ArrayUtils.asImmutableList("1-8", "2-8", "3-8");
    List<String> PIRATE_MAPS       = ArrayUtils.asImmutableList("5-1", "5-2", "5-3");
    List<String> BLACK_LIGHT_MAPS  = ArrayUtils.asImmutableList("1BL", "2BL", "3BL");

    /**
     * @return current {@link Map}
     */
    Map getCurrentMap();

    /**
     * @return bounds of the current map
     */
    Area.Rectangle getCurrentMapBounds();

    /**
     * @return {@link Collection} of all known maps
     */
    Collection<Map> getMaps();

    /**
     * Find {@link Map} by given {@code mapId}.
     *
     * @param mapId to find
     * @return {@link Map} with given {@code mapId}
     * @throws MapNotFoundException if map was not found
     */
    Map getById(int mapId) throws MapNotFoundException;

    /**
     * Find {@link Map} by given {@code mapId} otherwise will create a new one with given mapId.
     *
     * @param mapId to find
     * @return {@link Map} with given {@code mapId}
     */
    Map getOrCreateMapById(int mapId);

    /**
     * Find {@link Map} by given {@code mapName}.
     * {@code mapName} must equals searched {@link Map#getName()}
     *
     * @param mapName to find
     * @return {@link Map} with given {@code mapName}
     * @throws MapNotFoundException if map was not found
     */
    Map getByName(String mapName) throws MapNotFoundException;

    /**
     * Given {@link Listener} will be executed on each map change.
     * <p>
     * Every {@link Listener} need to have strong reference.
     *
     * @param onMapChange to be added
     * @return given {@link Listener} reference
     * @see Listener
     */
    Listener<Map> addMapChangeListener(Listener<Map> onMapChange);

    /**
     * @return best {@link Portal} which leads to {@code targetMap}
     */
    Portal findNext(Map targetMap);

    class MapNotFoundException extends Exception {
        public MapNotFoundException(int mapId) {
            super("Map with id " + mapId + " was not found");
        }

        public MapNotFoundException(String mapName) {
            super("Map " + mapName + " was not found");
        }
    }
}
