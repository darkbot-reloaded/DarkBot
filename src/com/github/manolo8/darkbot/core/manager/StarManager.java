package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Map;

import java.util.HashMap;

public class StarManager {

    public HashMap<Integer, Map> starSystem;
    public HashMap<String, Map> starSystemName;
    private HashMap<Integer, Portal> portals;

    public StarManager() {

        starSystem = new HashMap<>();
        starSystemName = new HashMap<>();
        portals = new HashMap<>();

        //MMO --------------------------------------------------------

        //1-1
        add(1, "1-1", new Portal(0, 150000159, 2));

        //1-2
        add(2, "1-2",
                new Portal(0, 150000160, 1),
                new Portal(0, 150000161, 3),
                new Portal(0, 150000163, 4)
        );

        //1-3
        add(3, "1-3",
                new Portal(0, 150000162, 2),
                new Portal(0, 150000185, 4),
                new Portal(0, 150000165, 7)
        );

        //1-4
        add(4, "1-4",
                new Portal(0, 150000164, 2),
                new Portal(0, 150000186, 3),
                new Portal(0, 150000189, 13),
                new Portal(0, 150000169, 12)
        );

        //1-5
        add(17, "1-5",
                new Portal(0, 150000299, 16),
                new Portal(0, 150000328, 29),
                new Portal(0, 150000300, 18),
                new Portal(0, 150000302, 19)
        );

        //1-6
        add(18, "1-6",
                new Portal(0, 150000301, 17),
                new Portal(0, 150000304, 20)
        );

        //1-7
        add(19, "1-7",
                new Portal(0, 150000306, 20),
                new Portal(0, 150000303, 17)
        );

        //1-8
        add(20, "1-8",
                new Portal(0, 150000305, 18),
                new Portal(0, 150000307, 19)
        );

        //MMO --------------------------------------------------------

        //EIC --------------------------------------------------------

        //2-1
        add(5, "2-1", new Portal(0, 150000174, 6));

        //2-2
        add(6, "2-2",
                new Portal(0, 150000168, 7),
                new Portal(0, 150000175, 8),
                new Portal(0, 150000173, 5)
        );

        //2-3
        add(7, "2-3",
                new Portal(0, 150000166, 3),
                new Portal(0, 150000183, 8),
                new Portal(0, 150000167, 6)
        );

        //2-4
        add(8, "2-4",
                new Portal(0, 150000184, 7),
                new Portal(0, 150000191, 14),
                new Portal(0, 150000176, 6),
                new Portal(0, 150000177, 11)
        );

        //2-5
        add(21, "2-5",
                new Portal(0, 150000330, 16),
                new Portal(0, 150000309, 29),
                new Portal(0, 150000310, 22),
                new Portal(0, 150000312, 23)
        );

        //2-6
        add(22, "2-6",
                new Portal(0, 150000311, 21),
                new Portal(0, 150000314, 24)
        );

        //2-7
        add(23, "2-7",
                new Portal(0, 150000313, 21),
                new Portal(0, 150000316, 24)
        );

        //2-8
        add(24, "2-8",
                new Portal(0, 150000315, 22),
                new Portal(0, 150000317, 23)
        );

        //EIC --------------------------------------------------------

        //VRU --------------------------------------------------------

        //3-1
        add(9, "3-1", new Portal(0, 150000182, 10));

        //3-2
        add(10, "3-2",
                new Portal(0, 150000180, 11),
                new Portal(0, 150000172, 12),
                new Portal(0, 150000181, 9)
        );

        //3-3
        add(11, "3-3",
                new Portal(0, 150000178, 8),
                new Portal(0, 150000188, 12),
                new Portal(0, 150000179, 10)
        );

        //3-4
        add(12, "3-4",
                new Portal(0, 150000170, 4),
                new Portal(0, 150000193, 15),
                new Portal(0, 150000187, 11),
                new Portal(0, 150000171, 10)
        );

        //3-5
        add(25, "3-5",
                new Portal(0, 150000319, 16),
                new Portal(0, 150000332, 29),
                new Portal(0, 150000320, 26),
                new Portal(0, 150000322, 27)
        );

        //3-6
        add(26, "3-6",
                new Portal(0, 150000321, 25),
                new Portal(0, 150000324, 28)
        );

        //3-7
        add(27, "3-7",
                new Portal(0, 150000323, 25),
                new Portal(0, 150000326, 28)
        );

        //3-8
        add(28, "3-8",
                new Portal(0, 150000327, 27),
                new Portal(0, 150000325, 26)
        );
        //VRU --------------------------------------------------------

        //PVP --------------------------------------------------------

        //4-1
        add(13, "4-1",
                new Portal(0, 150000190, 4),
                new Portal(0, 150000195, 14),
                new Portal(0, 150000200, 15),
                new Portal(0, 150000289, 16)
        );

        //4-2
        add(14, "4-2",
                new Portal(0, 150000192, 8),
                new Portal(0, 150000196, 13),
                new Portal(0, 150000197, 15),
                new Portal(0, 150000291, 16)
        );

        //4-3
        add(15, "4-3",
                new Portal(0, 150000194, 12),
                new Portal(0, 150000198, 14),
                new Portal(0, 150000199, 13),
                new Portal(0, 150000293, 16)
        );

        //4-4
        add(16, "4-4",
                new Portal(0, 150000318, 25),
                new Portal(0, 150000294, 15),
                new Portal(0, 150000292, 14),
                new Portal(0, 150000308, 21),
                new Portal(0, 150000298, 17),
                new Portal(0, 150000290, 13)
        );

        //4-5
        add(29, "4-5",
                new Portal(0, 150000329, 17),
                new Portal(0, 150000331, 21),
                new Portal(0, 150000333, 15)
        );
        //PVP --------------------------------------------------------

        fill();
    }

    private void add(int id, String name, Portal... portals) {
        Map map = new Map(id, name, portals);
        this.starSystem.put(id, map);
        this.starSystemName.put(map.name, map);
        for (Portal portal : portals) {
            this.portals.put(portal.id, portal);
        }
    }

    private void fill() {
        for (Portal portal : portals.values()) {
            portal.target = starSystem.get(portal.targetId);
        }

        for (Map map : starSystem.values()) {
            map.recursiveDistance();
        }
    }

    public Map fromId(int id) {
        Map map = starSystem.get(id);

        if (map == null) {
            map = new Map(id, "Unknown map " + id, new Portal[0]);
        }

        return map;
    }

    public Portal fromIdPortal(int id) {
        Portal portal = portals.get(id);

        if (portal == null) {
            portal = new Portal(0, id, 0);
        }

        return portal;
    }

    public Portal next(Map current, Location location, Map target) {

        Portal closest = null;

        double distance = 1000000;
        int min = -1;

        for (Portal portal : current.portals) {
            int dist = target.distances.get(portal.target);

            if (min == -1 || dist <= min) {

                if (dist != min || distance > portal.location.distance(location)) {
                    min = dist;
                    closest = portal;
                    distance = portal.location.distance(location);
                }

            }

        }

        return closest;
    }

    public Map fromName(String name) {
        return starSystemName.get(name);
    }
}
