package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.entities.Portal;

import java.util.HashMap;

public class Map {

    public int id;
    public String name;
    public Portal[] portals;
    public HashMap<Map, Integer> distances;
    public boolean pvp;

    public Map(int id, String name, boolean pvp, Portal[] portals) {
        this.id = id;
        this.name = name;
        this.portals = portals;
        this.distances = new HashMap<>();
        this.pvp = pvp;
    }

    public void recursiveDistance() {
        putDistance(this, 0);
    }

    private void putDistance(Map map, int distance) {
        if (distances.containsKey(map)) {
            int value = distances.get(map);
            if (value > distance) {
                distances.put(map, distance);

                for (Portal portal : portals) {
                    portal.target.putDistance(map, distance + 1);
                }

            }
        } else {
            distances.put(map, distance);

            for (Portal portal : portals) {
                portal.target.putDistance(map, distance + 1);
            }
        }
    }
}
