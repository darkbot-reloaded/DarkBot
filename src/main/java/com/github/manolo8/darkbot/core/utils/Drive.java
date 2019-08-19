package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.MouseManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;
import com.github.manolo8.darkbot.utils.MathUtils;
import org.jgrapht.Graph;
import org.jgrapht.alg.tour.PalmerHamiltonianCycle;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.random;

public class Drive {

    private static final Random RANDOM = new Random();
    private boolean force = false;

    private final MapManager map;
    private final MouseManager mouse;
    private final HeroManager hero;
    private final LocationInfo heroLoc;

    public PathFinder pathFinder;
    public LinkedList<PathPoint> paths = new LinkedList<>();

    private Location tempDest, endLoc, lastSegment;

    private long lastClick;
    public long lastMoved;

    public Drive(HeroManager hero, MapManager map) {
        this.map = map;
        this.mouse = new MouseManager(map);
        this.hero = hero;
        this.heroLoc = hero.locationInfo;
        this.pathFinder = new PathFinder(map);
    }

    public void checkMove() {
        // Path-finder changed and bot is already traveling, re-create route
        if (endLoc != null && pathFinder.changed() && tempDest == null) tempDest = endLoc;

        boolean newPath = tempDest != null;
        if (newPath) { // Calculate new path
            if (endLoc == null) endLoc = tempDest; // Should never happen
            paths = pathFinder.createRote(heroLoc.now, tempDest);
            tempDest = null;
        }

        if (paths.isEmpty() || !heroLoc.isLoaded())
            return;

        lastMoved = System.currentTimeMillis();

        Location now = heroLoc.now, last = heroLoc.last, next = current();
        if (next == null) return;
        newPath |= !next.equals(lastSegment);
        lastSegment = next;

        if (hero.timeTo(now.distance(next)) > 100) {
            boolean diffAngle = MathUtils.angleDiff(heroLoc.angle, next.angle(last)) > 0.015;
            if (heroLoc.isMoving() && !diffAngle) {
                if (System.currentTimeMillis() - lastClick > 2000) click(next);
                return;
            }

            if (!force && heroLoc.isMoving() && !newPath && System.currentTimeMillis() - lastClick > 350) stop(false);
            else {
                if (!newPath && System.currentTimeMillis() - lastClick > 300) tempDest = endLoc; // Re-calculate path next tick
                click(next);
            }
        } else {
            synchronized (Main.UPDATE_LOCKER) {
                paths.removeFirst();
            }
            if (paths.isEmpty()) {
                if (this.endLoc.equals(lastRandomMove)) lastRandomMove = null;
                this.endLoc = this.tempDest = null;
            }
        }
    }

    private Location current() {
        if (paths.isEmpty()) return null;
        PathPoint point = paths.getFirst();
        return new Location(point.x, point.y);
    }

    private void click(Location loc) {
        if (System.currentTimeMillis() - lastClick > 300) {
            lastClick = System.currentTimeMillis();
            mouse.clickLoc(loc);
        }
    }

    public boolean canMove(Location location) {
        return !map.isOutOfMap(location.x, location.y) && pathFinder.canMove((int) location.x, (int) location.y);
    }

    public double closestDistance(Location location) {
        PathPoint closest = pathFinder.fixToClosest(new PathPoint((int) location.x, (int) location.y));
        return location.distance(closest.toLocation());
    }

    public double distanceBetween(Location loc, int x, int y) {
        double sum = 0;
        PathPoint begin = new PathPoint((int) loc.x, (int) loc.y);
        for (PathPoint curr : pathFinder.createRote(begin, new PathPoint(x, y)))
            sum += Math.sqrt(Math.pow(begin.x - curr.x, 2) + Math.pow(begin.y - curr.y, 2));
        return sum;
    }

    public void toggleRunning(boolean running) {
        this.force = running;
        stop(true);
    }

    public void stop(boolean current) {
        if (heroLoc.isMoving() && current) {
            Location stopLoc = heroLoc.now.copy();
            stopLoc.toAngle(heroLoc.now, heroLoc.last.angle(heroLoc.now), 100);
            mouse.clickLoc(stopLoc);
        }

        endLoc = tempDest = null;
        if (!paths.isEmpty()) paths = new LinkedList<>();
    }

    public void clickCenter(boolean single, Location aim) {
        mouse.clickCenter(single, aim);
    }

    public void move(Entity entity) {
        move(entity.locationInfo.now);
    }

    public void move(Location location) {
        move(location.x, location.y);
    }

    public void move(double x, double y) {
        tempDest = endLoc = new Location(x, y);
    }

    private Location lastRandomMove;
    private List<ZoneInfo.Zone> lastZones;
    private int lastZoneIdx;
    public void moveRandom() {
        ZoneInfo area = map.preferred;
        boolean sequential = ConfigEntity.INSTANCE.getConfig().GENERAL.ROAMING.SEQUENTIAL;

        List<ZoneInfo.Zone> zones = sequential ? area.getSortedZones() : area.getZones();
        boolean changed = !zones.equals(lastZones);
        lastZones = zones;

        if (ConfigEntity.INSTANCE.getConfig().GENERAL.ROAMING.KEEP && !changed && lastRandomMove != null) {
            move(lastRandomMove);
            return;
        }

        if (changed && !lastZones.isEmpty()) {
            Location search = lastRandomMove != null ? lastRandomMove : movingTo();
            ZoneInfo.Zone closest = lastZones.stream().min(Comparator.comparingDouble(zone ->
                    zone.innerPoint(0.5, 0.5, MapManager.internalWidth, MapManager.internalHeight).distance(search))).orElse(null);
            lastZoneIdx = lastZones.indexOf(closest);
        }

        if (lastZones.isEmpty()) {
            lastRandomMove = new Location(random() * MapManager.internalWidth, random() * MapManager.internalHeight);
        } else {
            if (lastZoneIdx >= lastZones.size()) lastZoneIdx = 0;
            ZoneInfo.Zone zone = lastZones.get(sequential ? lastZoneIdx++ : RANDOM.nextInt(zones.size()));

            lastRandomMove = zone.innerPoint(random(), random(), MapManager.internalWidth, MapManager.internalHeight);
        }
        move(lastRandomMove);
    }

    public boolean isMoving() {
        return !paths.isEmpty() || heroLoc.isMoving();
    }

    public Location movingTo() {
        return endLoc == null ? heroLoc.now.copy() : endLoc.copy();
    }

    public boolean isOutOfMap() {
        return map.isOutOfMap(heroLoc.now.x, heroLoc.now.y);
    }
}
