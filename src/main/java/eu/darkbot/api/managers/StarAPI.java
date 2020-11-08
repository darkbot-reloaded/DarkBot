package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * API to add, manage, retrieve maps.
 */
public interface StarAPI extends API {
    List<String> HOME_MAPS = ArrayUtils.asImmutableList("1-1", "2-1", "3-1");
    List<String> OUTPOST_HOME_MAPS = ArrayUtils.asImmutableList("1-8", "2-8", "3-8");

    int getMapWidth();
    int getMapHeight();

    /**
     * @param listener hard ref listener otherwise will be GC
     */
    // TODO: 08.11.2020
    void addMapChangeListener(MapChangeListener listener);

    Map getCurrentMap();

    /**
     * Adds given map into list of maps.
     * @return true if was added, false when list already contains given {@link Map}
     */
    boolean addMap(@NotNull Map map);

    /**
     * @return {@link Set} of all known maps
     */
    Set<Map> getMaps();

    /**
     * Find {@link Map} by given {@code mapId}.
     *
     * @param mapId to find
     * @return {@link Map} with given {@code mapId}
     * @throws MapNotFoundException if map was not found
     */
    Map getById(int mapId) throws MapNotFoundException;

    /**
     * Find {@link Map} by given {@code mapName}.
     * {@code mapName} must equals searched {@link Map#getName()}
     *
     * @param mapName to find
     * @return {@link Map} with given {@code mapName}
     * @throws MapNotFoundException if map was not found
     */
    Map getByName(String mapName) throws MapNotFoundException;

    class MapNotFoundException extends Exception {
        public MapNotFoundException(int mapId) {
            super("Map with id " + mapId + " was not found");
        }

        public MapNotFoundException(String mapName) {
            super("Map " + mapName + " was not found");
        }
    }

    interface MapChangeListener {

        void onMapChange(Map map);
    }
}
