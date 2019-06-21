package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.objects.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DirectedPseudograph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StarBuilder {

    private class TempMap {
        private int id;
        private String name;
        private boolean isGG;
        private List<TempPort> ports = new ArrayList<>();

        TempMap(int id, String name, boolean isGG) {
            this.id = id;
            this.name = name;
            this.isGG = isGG;
        }
    }

    private class TempPort {
        private int x, y, factionId;
        private String targetMap;

        TempPort(int x, int y, String targetMap, int factionId) {
            this.x = x;
            this.y = y;
            this.targetMap = targetMap;
            this.factionId = factionId;
        }
    }

    private class GGPort {
        private int type;
        private boolean looped;
        private String[] maps;
        private String targetMap = current.name;

        public GGPort(int type, boolean looped, String[] maps) {
            this.type = type;
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

    protected StarBuilder addGG(int id, String name) {
        return setCurrent(new TempMap(id, name, true));
    }

    private StarBuilder setCurrent(TempMap map) {
        this.current = map;
        maps.add(map);
        return this;
    }

    protected StarBuilder addPortal(int x, int y, String targetMap) {
        current.ports.add(new TempPort(x, y, targetMap, -1));
        return this;
    }

    @SuppressWarnings("SameParameterValue")
    protected StarBuilder addPortal(int x, int y, String targetMap, int factionId) {
        current.ports.add(new TempPort(x, y, targetMap, factionId));
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
     * Adds a type portal to jump to the gate, from one of the maps, but won't be able to jump from itself (Special cases).
     */
    protected StarBuilder accessOnlyBy(int type, String... maps) {
        this.ggPorts.add(new GGPort(type, false, maps));
        return this;
    }

    public Graph<Map, Portal> build() {
        DirectedPseudograph<Map, Portal> graph = new DirectedPseudograph<>(Portal.class);
        HashMap<String, Map> mapsByName = new HashMap<>();
        for (TempMap tmpMap : maps) {
            Map map = new Map(tmpMap.id, tmpMap.name, tmpMap.name.startsWith("4-"), tmpMap.isGG);
            mapsByName.put(map.name, map);
            graph.addVertex(map);
        }

        for (TempMap map : maps) {
            for (TempPort port : map.ports) {
                Map target = mapsByName.get(port.targetMap);
                graph.addEdge(mapsByName.get(map.name), target, new Portal(-1, port.x, port.y, target, port.factionId));
            }
        }

        for (GGPort ggPort : ggPorts) {
            Map gg = mapsByName.get(ggPort.targetMap);
            if (ggPort.looped) graph.addEdge(gg, gg, new Portal(ggPort.type, -1, -1, gg, -1));
            for (String mapName : ggPort.maps) {
                Map from = mapsByName.get(mapName), to = mapsByName.get(ggPort.targetMap);
                graph.addEdge(from, to, new Portal(ggPort.type, -1, -1, gg, -1));
            }
        }
        return graph;
    }

}
