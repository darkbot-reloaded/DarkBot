package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathPoint;

import java.util.List;
import java.util.Random;

import static java.lang.Math.random;

public class Drive {

    private static final Random RANDOM = new Random();
    private boolean force = false;

    private final MapManager map;
    private final HeroManager hero;
    private final LocationInfo heroLoc;

    public PathFinder pathFinder;

    private Location tempDest, endLoc, lastSegment;

    public long lastClick, lastMoved;

    public Drive(HeroManager hero, MapManager map) {
        this.map = map;
        this.hero = hero;
        this.heroLoc = hero.locationInfo;
        this.pathFinder = new PathFinder(map);
    }

    public void checkMove() {
        if (endLoc != null && pathFinder.changed() && tempDest == null) tempDest = endLoc;

        boolean newPath = tempDest != null;
        if (tempDest != null) {
            pathFinder.createRote(heroLoc.now, tempDest);
            tempDest = null;
        }

        if (pathFinder.isEmpty() || !heroLoc.isLoaded())
            return;

        lastMoved = System.currentTimeMillis();

        Location now = heroLoc.now, last = heroLoc.last, next = pathFinder.current();
        newPath |= !next.equals(lastSegment);
        lastSegment = next;

        boolean diffAngle = Math.abs(now.angle(next) - last.angle(now)) > 0.08;
        if (hero.timeTo(now.distance(next)) > 100 || diffAngle) {
            if (heroLoc.isMoving() && !diffAngle) return;

            if (!force && heroLoc.isMoving() && !newPath && System.currentTimeMillis() - lastClick > 500) stop(false);
            else click(next);
        } else {
            pathFinder.currentCompleted();
            if (pathFinder.isEmpty()) this.endLoc = null;
        }
    }

    private void click(Location loc) {
        if (System.currentTimeMillis() - lastClick > 300) {
            lastClick = System.currentTimeMillis();
            map.mouseClick(loc);
        }
    }

    public boolean canMove(Location location) {
        return !map.isOutOfMap(location.x, location.y) && pathFinder.canMove((int) location.x, (int) location.y);
    }

    public double closestDistance(Location location) {
        return location.distance(pathFinder.fixToClosest(new PathPoint((int) location.x, (int) location.y)).toLocation());
    }

    public void toggleRunning(boolean running) {
        this.force = running;
        stop(true);
    }

    public void stop(boolean current) {
        if (heroLoc.isMoving() && current) {
            Location stopLoc = heroLoc.now.copy();
            stopLoc.toAngle(heroLoc.now, heroLoc.last.angle(heroLoc.now), 100);
            map.mouseClick(stopLoc);
        }

        endLoc = null;
        if (!pathFinder.isEmpty()) pathFinder.path().clear();
    }

    public void clickCenter(boolean single) {
        map.clickCenter(single);
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

    public void moveRandom() {
        ZoneInfo area = map.preferred;
        List<ZoneInfo.Zone> zones = area.getZones();
        if (zones.isEmpty()) {
            move(random() * MapManager.internalWidth, random() * MapManager.internalHeight);
        } else {
            ZoneInfo.Zone zone = zones.get(RANDOM.nextInt(zones.size()));
            double cellSize = 1d / area.resolution;
            double xProportion = (zone.x / (double) area.resolution) + random() * cellSize,
                    yProportion = (zone.y / (double) area.resolution) + random() * cellSize;

            move(xProportion * MapManager.internalWidth, yProportion * MapManager.internalHeight);
        }
    }

    public boolean isMoving() {
        return !pathFinder.isEmpty() || heroLoc.isMoving();
    }

    public Location movingTo() {
        return tempDest == null ? heroLoc.now.copy() : tempDest.copy();
    }

    public boolean isOutOfMap() {
        return map.isOutOfMap(heroLoc.now.x, heroLoc.now.y);
    }
}
