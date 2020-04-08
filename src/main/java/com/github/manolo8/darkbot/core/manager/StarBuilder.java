package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.objects.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StarBuilder {

    private static class TempMap {
        private int id;
        private String name, shortName;
        private boolean isGG;
        private List<TempPort> ports = new ArrayList<>();

        TempMap(int id, String name, boolean isGG) {
            this(id, name, name, isGG);
        }
        TempMap(int id, String name, String shortName, boolean isGG) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
            this.isGG = isGG;
        }
    }

    private static class TempPort {
        private final int x, y, type, factionId;
        private final String targetMap;

        TempPort(int x, int y, int type, int factionId, String targetMap) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.factionId = factionId;
            this.targetMap = targetMap;
        }
    }

    private class GGPort {
        private int type;
        private boolean looped;
        private String[] maps;
        private int x, y;
        private String targetMap = current.name;

        public GGPort(int type, boolean looped, String[] maps) {
            this.type = type;
            this.x = this.y = -1;
            this.looped = looped;
            this.maps = maps;
        }
        public GGPort(int type, int x, int y, boolean looped, String[] maps) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.looped = looped;
            this.maps = maps;
        }
    }

    private TempMap current;
    private List<TempMap> maps = new ArrayList<>();
    private List<GGPort> ggPorts = new ArrayList<>();

    protected StarBuilder addMap(int id, String name) {
        return setCurrent(new TempMap(id, name, false));
    }

    protected StarBuilder addMap(int id, String name, String shortName) {
        return setCurrent(new TempMap(id, name, shortName, false));
    }

    protected StarBuilder addGG(int id, String name) {
        String shortName = name;
        if (name.matches("GG .{0,3} [0-9]+")) {
            shortName = name.substring(0, name.lastIndexOf(" "));
        }
        return setCurrent(new TempMap(id, name, shortName, true));
    }

    protected StarBuilder addGG(int id, String name, String shortName) {
        return setCurrent(new TempMap(id, name, shortName, true));
    }

    private StarBuilder setCurrent(TempMap map) {
        this.current = map;
        maps.add(map);
        return this;
    }

    protected StarBuilder addPortal(int x, int y, String targetMap) {
        current.ports.add(new TempPort(x, y, -1, -1, targetMap));
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    protected StarBuilder addPortal(int x, int y, String targetMap, int factionId) {
        current.ports.add(new TempPort(x, y, -1, factionId, targetMap));
        return this;
    }

    /**
     * Adds a type portal to jump to the gate, from one of the maps, and also includes itself (Like most ggs).
     */
    protected StarBuilder accessBy(int type, String... maps) {
        this.ggPorts.add(new GGPort(type, true, maps));
        return this;
    }

    /**
     * Adds a type portal to jump to the gate, from one of the maps, and also includes itself (Like most ggs).
     */
    protected StarBuilder accessBy(int type, int x, int y, String... maps) {
        this.ggPorts.add(new GGPort(type, x, y, true, maps));
        return this;
    }

    /**
     * Adds a type portal to jump to the gate, from one of the maps, but won't be able to jump from itself (Special cases).
     */
    protected StarBuilder accessOnlyBy(int type, String... maps) {
        this.ggPorts.add(new GGPort(type, false, maps));
        return this;
    }
    /**
     * Adds a type portal to jump to the gate, from one of the maps, but won't be able to jump from itself (Special cases).
     */
    protected StarBuilder accessOnlyBy(int type, int x, int y, String... maps) {
        this.ggPorts.add(new GGPort(type, x, y, false, maps));
        return this;
    }

    /**
     * Adds an exit portal to a galaxy gate map
     */
    protected StarBuilder exitBy(int type) {
        current.ports.add(new TempPort(-1, -1, type, -1, "Home Map"));
        return this;
    }

    public Graph<Map, Portal> build() {
        DirectedPseudograph<Map, Portal> graph = new DirectedPseudograph<>(Portal.class);
        HashMap<String, Map> mapsByName = new HashMap<>();
        for (TempMap tmpMap : maps) {
            Map map = new Map(tmpMap.id, tmpMap.name, tmpMap.shortName, tmpMap.name.startsWith("4-"), tmpMap.isGG);
            mapsByName.put(map.name, map);
            graph.addVertex(map);
        }

        for (TempMap map : maps) {
            for (TempPort port : map.ports) {
                Map target = mapsByName.get(port.targetMap);
                graph.addEdge(mapsByName.get(map.name), target, new Portal(port.type, port.x, port.y, target, port.factionId));
            }
        }

        for (GGPort ggPort : ggPorts) {
            Map gg = mapsByName.get(ggPort.targetMap);
            if (ggPort.looped) graph.addEdge(gg, gg, new Portal(ggPort.type, -1, -1, gg, -1));
            for (String mapName : ggPort.maps) {
                Map from = mapsByName.get(mapName);
                graph.addEdge(from, gg, new Portal(ggPort.type, ggPort.x, ggPort.y, gg, -1));
            }
        }
        return graph;
    }

}
