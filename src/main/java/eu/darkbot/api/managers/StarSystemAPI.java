package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.entities.utils.GameMap;
import eu.darkbot.api.events.Event;
import eu.darkbot.utils.ArrayUtils;

import java.util.Collection;
import java.util.List;

/**
 * API to add, manage, retrieve maps.
 */
public interface StarSystemAPI extends API.Singleton {
    List<String> HOME_MAPS         = ArrayUtils.asImmutableList("1-1", "2-1", "3-1");
    List<String> OUTPOST_HOME_MAPS = ArrayUtils.asImmutableList("1-8", "2-8", "3-8");
    List<String> PIRATE_MAPS       = ArrayUtils.asImmutableList("5-1", "5-2", "5-3");
    List<String> BLACK_LIGHT_MAPS  = ArrayUtils.asImmutableList("1BL", "2BL", "3BL");

    /**
     * @return current {@link GameMap}
     */
    GameMap getCurrentMap();

    /**
     * @return bounds of the current map
     */
    Area.Rectangle getCurrentMapBounds();

    /**
     * @return {@link Collection} of all known maps
     */
    Collection<? extends GameMap> getMaps();

    /**
     * Find {@link GameMap} by given {@code mapId}.
     *
     * @param mapId to find
     * @return {@link GameMap} with given {@code mapId}
     * @throws MapNotFoundException if map was not found
     */
    GameMap getById(int mapId) throws MapNotFoundException;

    /**
     * Find {@link GameMap} by given {@code mapId} otherwise will create a new one with given mapId.
     *
     * @param mapId to find
     * @return {@link GameMap} with given {@code mapId}
     */
    GameMap getOrCreateMapById(int mapId);

    /**
     * Find {@link GameMap} by given {@code mapName}.
     * {@code mapName} must equals searched {@link GameMap#getName()}
     *
     * @param mapName to find
     * @return {@link GameMap} with given {@code mapName}
     * @throws MapNotFoundException if map was not found
     */
    GameMap getByName(String mapName) throws MapNotFoundException;

    /**
     * @return best {@link Portal} which leads to {@code targetMap}
     */
    Portal findNext(GameMap targetMap);

    class MapNotFoundException extends Exception {
        public MapNotFoundException(int mapId) {
            super("Map with id " + mapId + " was not found");
        }

        public MapNotFoundException(String mapName) {
            super("Map " + mapName + " was not found");
        }
    }

    class MapChangeEvent implements Event {
        private final GameMap previous, next;

        public MapChangeEvent(GameMap previous, GameMap next) {
            this.previous = previous;
            this.next = next;
        }

        public GameMap getPrevious() {
            return previous;
        }

        public GameMap getNext() {
            return next;
        }
    }
}
