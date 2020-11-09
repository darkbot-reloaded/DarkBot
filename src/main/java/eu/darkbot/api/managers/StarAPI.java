package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.api.objects.Locatable;
import eu.darkbot.api.objects.Rectangle;
import eu.darkbot.api.utils.ChangeListener;
import eu.darkbot.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * API to add, manage, retrieve maps.
 */
public interface StarAPI extends API {
    List<String> HOME_MAPS = ArrayUtils.asImmutableList("1-1", "2-1", "3-1");
    List<String> OUTPOST_HOME_MAPS = ArrayUtils.asImmutableList("1-8", "2-8", "3-8");

    /**
     * @return current {@link Map}
     */
    Map getCurrentMap();

    /**
     * @return bounds of the current map
     */
    Rectangle getCurrentMapBounds();

    /**
     * Adds given map into list of maps.
     *
     * @return true if was added, false when list already contains given {@link Map}
     */
    boolean addMap(@NotNull Map map);

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
     * Find {@link Map} by given {@code mapName}.
     * {@code mapName} must equals searched {@link Map#getName()}
     *
     * @param mapName to find
     * @return {@link Map} with given {@code mapName}
     * @throws MapNotFoundException if map was not found
     */
    Map getByName(String mapName) throws MapNotFoundException;

    /**
     * Given {@link ChangeListener} will be executed on each map change.
     * <p>
     * Every {@link ChangeListener} need to have strong reference.
     *
     * @param listener to be added
     * @return given {@code listener} reference
     * @see ChangeListener
     */
    ChangeListener<Map> addMapChangeListener(ChangeListener<Map> listener);

    class MapNotFoundException extends Exception {
        public MapNotFoundException(int mapId) {
            super("Map with id " + mapId + " was not found");
        }

        public MapNotFoundException(String mapName) {
            super("Map " + mapName + " was not found");
        }
    }

}
