package eu.darkbot.api.managers;

import eu.darkbot.api.entities.utils.Map;

import java.util.Set;

public interface StarManager {
    String[] HOME_MAPS = new String[]{"1-1", "2-1", "3-1"};
    String[] OUTPOST_HOME_MAPS = new String[]{"1-8", "2-8", "3-8"};

    Map addMap(Map map);
    Set<Map> getMaps();

    default Map getById(int mapId) throws MapNotFoundException {
        return getMaps().stream()
                .filter(map -> map.getId() == mapId)
                .findAny()
                .orElseThrow(MapNotFoundException::new);
    }

    default Map getByName(String mapName) throws MapNotFoundException {
        return getMaps().stream()
                .filter(map -> map.getName().equals(mapName))
                .findAny()
                .orElseThrow(MapNotFoundException::new);
    }

    class MapNotFoundException extends Exception {
        public MapNotFoundException() {
        }
    }
}
