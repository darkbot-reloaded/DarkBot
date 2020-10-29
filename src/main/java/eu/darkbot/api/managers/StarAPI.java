package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.entities.utils.Map;
import eu.darkbot.utils.ArrayUtils;

import java.util.List;
import java.util.Set;

public interface StarAPI extends API {
    List<String> HOME_MAPS = ArrayUtils.asImmutableList("1-1", "2-1", "3-1");
    List<String> OUTPOST_HOME_MAPS = ArrayUtils.asImmutableList("1-8", "2-8", "3-8");

    Map addMap(Map map);

    Set<Map> getMaps();

    default Map getById(int mapId) throws MapNotFoundException {
        return getMaps().stream()
                .filter(map -> map.getId() == mapId)
                .findAny()
                .orElseThrow(() -> new MapNotFoundException(mapId));
    }

    default Map getByName(String mapName) throws MapNotFoundException {
        return getMaps().stream()
                .filter(map -> map.getName().equals(mapName))
                .findAny()
                .orElseThrow(() -> new MapNotFoundException(mapName));
    }

    class MapNotFoundException extends Exception {
        public MapNotFoundException(int mapId) {
            super("Map with id " + mapId + " was not found");
        }

        public MapNotFoundException(String mapName) {
            super("Map " + mapName + " was not found");
        }
    }
}
