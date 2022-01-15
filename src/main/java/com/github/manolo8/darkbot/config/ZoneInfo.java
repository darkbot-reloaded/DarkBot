package com.github.manolo8.darkbot.config;

import com.github.manolo8.darkbot.config.utils.Ignorable;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.FixedTwoOptHeuristicTSP;
import eu.darkbot.api.game.other.Locatable;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.HamiltonianCycleAlgorithm;
import org.jgrapht.alg.tour.HeldKarpTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZoneInfo implements Serializable, Ignorable, eu.darkbot.api.config.types.ZoneInfo {
    private static final byte[] MASKS = new byte[8];
    static {
        for (int i = 0; i < MASKS.length; i++) MASKS[i] = (byte) (1 << i);
    }

    public int resolution = 1;
    private byte[] data;
    private transient List<Zone> zones;
    public transient boolean changed = true;
    private transient boolean ordered = false;

    public ZoneInfo() {}

    public ZoneInfo(int resolution) {
        this.resolution = resolution;
        data = new byte[((resolution * resolution) + 7) / 8];
    }

    @Override
    public boolean ignore() {
        if (!changed) return zones.isEmpty();
        for (int x = 0; x < resolution; x++)
            for (int y = 0; y < resolution; y++)
                if (get(x, y)) return false;
        return true;
    }

    @Override
    public boolean writeAsNull() {
        return true;
    }

    @Override
    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        if (resolution == this.resolution) return;
        ZoneInfo newZone = new ZoneInfo(resolution);

        for (int x = 0; x < resolution; x++)
            for (int y = 0; y < resolution; y++)
                if (get(x, y)) newZone.set(x, y);

        this.resolution = resolution;
        this.data = newZone.data;
    }

    public boolean get(int x, int y) {
        if (outside(x, y)) return false;
        int pos = x + (y * resolution);
        return (data[pos / 8] & MASKS[pos % 8]) != 0;
    }

    public void set(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] |= MASKS[pos % 8];
        changed = true;
    }

    public void remove(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] &= ~MASKS[pos % 8];
        changed = true;
    }

    public void toggle(int x, int y) {
        if (outside(x, y)) return;
        int pos = x + (y * resolution);
        data[pos / 8] ^= MASKS[pos % 8];
        changed = true;
    }

    public void set(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) set(x, currY);
    }

    public void remove(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) remove(x, currY);
    }

    public void toggle(int x, int y, int x2, int y2) {
        for (; x < x2; x++) for (int currY = y; currY < y2; currY++) toggle(x, currY);
    }

    public void set(int x, int y, int x2, int y2, boolean state) {
        if (state) set(x, y, x2, y2);
        else remove(x, y, x2, y2);
    }

    public boolean outside(int x, int y) {
        return x < 0 || y < 0 || x >= resolution || y >= resolution;
    }

    public boolean contains(double xPercent, double yPercent) {
        return get(Math.min((int) (xPercent * resolution), resolution - 1), Math.min((int) (yPercent * resolution), resolution - 1));
    }

    public boolean contains(Location loc) {
        return contains((Locatable) loc);
    }

    public boolean contains(Locatable loc) {
        return contains(loc.getX() / MapManager.internalWidth, loc.getY() / MapManager.internalHeight);
    }

    public List<Zone> getZones() {
        if (changed) {
            zones = new ArrayList<>();
            for (int x = 0; x < resolution; x++) {
                for (int y = 0; y < resolution; y++)
                    if (get(x, y)) zones.add(new Zone(x, y));
            }
            changed = false;
            ordered = false;
        }
        return zones;
    }

    public List<Zone> getSortedZones() {
        getZones(); // Update zones
        if (!ordered) {
            ordered = true;
            if (zones.size() <= 1 || zones.size() > 1000) return zones;

            SimpleWeightedGraph<Zone, DefaultWeightedEdge> zoneGraph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            for (ZoneInfo.Zone zone : zones) zoneGraph.addVertex(zone); // Add vertex
            for (int i = 0; i < zones.size(); i++) {
                ZoneInfo.Zone from = zones.get(i);
                for (int j = i + 1; j < zones.size(); j++) { // Add connections
                    ZoneInfo.Zone to = zones.get(j);
                    zoneGraph.addEdge(from, to);
                    zoneGraph.setEdgeWeight(from, to, Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2)));
                }
            }
            HamiltonianCycleAlgorithm<Zone, DefaultWeightedEdge> alg;
            if (zones.size() <= 20) {
                // For small graphs, find the best path
                alg = new HeldKarpTSP<>();
            } else {
                // Make less passes the bigger the graph, keeping an ok time.
                alg = new FixedTwoOptHeuristicTSP<>(Math.max(Math.min(1500 - (zones.size() * 10), 750), 1));
            }
            GraphPath<Zone, DefaultWeightedEdge> path = alg.getTour(zoneGraph);
            zones = path.getVertexList();
            zones.remove(0);
        }
        return zones;
    }

    public class Zone {
        public int x, y;
        public Zone(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Finds the inner point for a width and height scale
         * @param xProp The 0-1 proportion in the X position inside the zone
         * @param yProp The 0-1 proportion in the Y position inside the zone
         * @param width The total width of the area this zone belongs to
         * @param height The total height of the area this zone belongs to
         * @return The location for the x & y proportion inside the zone, for the area this belongs to.
         */
        public Location innerPoint(double xProp, double yProp, double width, double height) {
            double cellSize = 1d / resolution;
            double xProportion = (x / (double) resolution) + xProp * cellSize,
                    yProportion = (y / (double) resolution) + yProp * cellSize;

            return new Location(xProportion * width, yProportion * height);
        }
    }

}
