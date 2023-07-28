package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.api.Capability;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.MouseManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.Locatable;
import eu.darkbot.api.managers.MovementAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.random;

// TODO: 07.06.2021 re-check, implement AbstractMovementApiImpl
public class Drive implements MovementAPI {

    private static final Random RANDOM = new Random();
    private boolean force = false;

    private final Main main;
    private final MapManager map;
    private final MouseManager mouse;
    private LocationInfo heroLoc = new LocationInfo();

    public PathFinder pathFinder;
    public LinkedList<Locatable> paths = new LinkedList<>();

    private Location tempDest, endLoc, lastSegment = new Location();

    private long lastDirChange;
    private long lastClick;
    public long lastMoved;

    public Drive(Main main, MapManager map, PathFinder pathFinder) {
        this.main = main;
        this.map = map;
        this.pathFinder = pathFinder;
        this.mouse = new MouseManager(map);
    }

    public void checkMove() {
        this.heroLoc = main.hero.locationInfo;

        // Pathfinder changed and bot is already traveling, re-create route
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
        if (newPath) {
            // If direction roughly similar, and changed dir little ago, and you're still gonna be moving, ignore change
            // This smooths out paths in short distances
            if (next.distance(lastSegment) + (System.currentTimeMillis() - lastDirChange) < 500 &&
                    main.hero.timeTo(now.distance(lastSegment)) > 50) return;
            lastDirChange = System.currentTimeMillis();
        }
        lastSegment = next;

        if (newPath || main.hero.timeTo(now.distance(next)) > 25) {
            double dirAngle = next.angle(last),
                    maxDiff = Math.max(0.02, MathUtils.angleDiff(next.angle(Location.of(heroLoc.last, dirAngle + (Math.PI / 2), 100)), dirAngle));
            if (!newPath && heroLoc.isMoving() && MathUtils.angleDiff(heroLoc.angle, dirAngle) < maxDiff) {
                if (System.currentTimeMillis() - lastDirChange > 2000) {
                    click(next);
                    lastDirChange =  System.currentTimeMillis();
                }
                return;
            }

            if (!force && heroLoc.isMoving() && System.currentTimeMillis() - lastDirChange > 350) stop(false);
            else {
                if (!newPath && System.currentTimeMillis() - lastDirChange > 300) tempDest = endLoc; // Re-calculate path next tick
                if (now.distance(next) < 100) click(Location.of(heroLoc.now, heroLoc.now.angle(next), 150));
                else click(next);
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
        Locatable point = paths.getFirst();
        return new Location(point.getX(), point.getY());
    }

    private void click(Location loc) {
        if (System.currentTimeMillis() - lastClick > 200) {
            lastClick = System.currentTimeMillis();

            if (Main.API.hasCapability(Capability.DIRECT_MOVE_SHIP)) Main.API.moveShip(loc);
            else mouse.clickLoc(loc);
        }
    }

    public boolean canMove(Location location) {
        return !map.isOutOfMap(location.x, location.y) && pathFinder.canMove(location.x, location.y);
    }

    @Deprecated /* Use getClosestDistance(Location) instead */
    public double closestDistance(Location location) {
        return getClosestDistance(location);
    }

    public double distanceBetween(Location loc, int x, int y) {
        return getDistanceBetween(loc.x, loc.y, x, y);
    }

    public void toggleRunning(boolean running) {
        this.force = running;
        stop(true);
    }

    @Override
    public void stop(boolean current) {
        if (heroLoc.isMoving() && current) {
            Location stopLoc = heroLoc.now.copy();
            stopLoc.toAngle(heroLoc.now, heroLoc.last.angle(heroLoc.now), 100);

            if (Main.API.hasCapability(Capability.DIRECT_MOVE_SHIP)) Main.API.moveShip(stopLoc);
            else mouse.clickLoc(stopLoc);
        }

        endLoc = tempDest = null;
        if (!paths.isEmpty()) paths = new LinkedList<>();
    }

    @Deprecated
    public void clickCenter(boolean single, Location aim) {
        mouse.clickCenter(single, aim);
    }

    public void move(Entity entity) {
        move(entity.locationInfo.now);
    }

    public void move(Location location) {
        move(location.x, location.y);
    }

    public void move(Locatable locatable) {
        move(locatable.getX(), locatable.getY());
    }

    public void move(double x, double y) {
        Location newDir = new Location(x, y);
        if (movingTo().distance(newDir) > 10) tempDest = endLoc = newDir;
    }

    private Location lastRandomMove;
    private List<ZoneInfo.Zone> lastZones;
    private int lastZoneIdx;

    @Override
    public void moveRandom() {
        ZoneInfo area = map.preferred;
        boolean sequential = main.config.GENERAL.ROAMING.SEQUENTIAL;

        List<ZoneInfo.Zone> zones = area == null ? Collections.emptyList() :
                sequential ? area.getSortedZones() : area.getZones();
        boolean changed = !zones.equals(lastZones);
        lastZones = zones;

        if (main.config.GENERAL.ROAMING.KEEP && !changed && lastRandomMove != null) {
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

    @Override
    public boolean isMoving() {
        return !paths.isEmpty() || heroLoc.isMoving();
    }

    @Override
    public boolean isMoving(long inTime) {
        return lastMoved + inTime >= System.currentTimeMillis();
    }

    public Location movingTo() {
        return endLoc == null ? heroLoc.now.copy() : endLoc.copy();
    }

    @Override
    public boolean isOutOfMap() {
        return map.isOutOfMap(heroLoc.now.x, heroLoc.now.y);
    }

    @Override
    public void jumpPortal(@NotNull Portal portal) {
        main.hero.jumpPortal(portal);
    }

    @Override
    public Location getDestination() {
        return movingTo();
    }

    @Override
    public Location getCurrentLocation() {
        return heroLoc.now;
    }

    @Override
    public @NotNull List<? extends Locatable> getPath() {
        return Collections.unmodifiableList(paths);
    }

    @Override
    public boolean canMove(double x, double y) {
        return !map.isOutOfMap(x, y) && pathFinder.canMove((int) x, (int) y);
    }

    @Override
    public void moveTo(double x, double y) {
        move(x, y);
    }

    @Override
    public double getClosestDistance(double x, double y) {
        Locatable from = Locatable.of(x, y);
        return from.distanceTo(pathFinder.fixToClosest(from));
    }

    @Override
    public double getDistanceBetween(double x, double y, double ox, double oy) {
        Locatable previous = Locatable.of(x, y);
        LinkedList<Locatable> path = pathFinder.createRote(previous, Locatable.of(ox, oy));
        double sum = 0;
        for (Locatable curr : path) {
            sum += previous.distanceTo(curr);
            previous = curr;
        }
        return sum;
    }

    @Override
    public boolean isInPreferredZone(Locatable locatable) {
        return map.preferred.contains(locatable);
    }

    public boolean movementInterrupted(long inTime) {
        return main.getGui().getMapDrawer().getLastMapClick() + inTime > System.currentTimeMillis();
    }
}
