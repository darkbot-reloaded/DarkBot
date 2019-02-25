package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Zone;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.pathfinder.PathFinder;

import java.util.List;
import java.util.Random;

import static java.lang.Math.min;
import static java.lang.Math.random;

public class Drive {

    private static final Random RANDOM = new Random();

    private final MapManager map;

    private final LocationInfo heroLocation;

    public PathFinder pathFinder;

    private Location destination;

    public long lastMoved;

    public Drive(HeroManager hero, MapManager map) {
        this.map = map;
        this.heroLocation = hero.locationInfo;
        this.pathFinder = new PathFinder(map.entities.obstacles);
    }

    public void checkMove() {

        if (destination != null) {
            pathFinder.createRote(heroLocation.now, destination);
            destination = null;
        }

        if (pathFinder.isEmpty() || !heroLocation.isLoaded())
            return;

        lastMoved = System.currentTimeMillis();

        Location now = heroLocation.now;
        Location destination = pathFinder.current();

        double distance = now.distance(destination);

        if (!heroLocation.isMoving())
            map.translateMousePress(now.x, now.y);

        if (distance > 100) {

            distance = min(distance, 200);

            double angle = destination.angle(now);

            map.translateMouseMove(
                    Math.cos(angle) * distance + now.x,
                    Math.sin(angle) * distance + now.y
            );

        } else {
            pathFinder.currentCompleted();
            if (pathFinder.isEmpty()) map.translateMouseMoveRelease(destination.x, destination.y);
        }

    }

    public boolean canMove(Location location) {
        return pathFinder.canMove(location);
    }

    public void stop(boolean current) {
        if (current) {
            map.translateMouseMoveRelease(heroLocation.now.x, heroLocation.now.y);
        }

        if (!pathFinder.isEmpty()) {
            pathFinder.path().clear();
        }
    }

    public void clickCenter(int times) {
        for (int i = 0; i < times; i++)
            map.translateMouseClick(heroLocation.now.x, heroLocation.now.y);
    }

    public void move(Entity entity) {
        move(entity.locationInfo.now);
    }

    public void move(Location location) {
        move(location.x, location.y);
    }

    public void move(double x, double y) {
        destination = new Location(x, y);
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
        return !pathFinder.isEmpty() || heroLocation.isMoving();
    }

    public boolean isOutOfMap() {
        return map.isOutOfMap(heroLocation.now.x, heroLocation.now.y);
    }
}
