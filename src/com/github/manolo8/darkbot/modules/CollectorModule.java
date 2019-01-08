package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

public class CollectorModule implements Module {

    private Main main;

    private List<Box> boxes;
    private List<Ship> ships;
    private List<BattleStation> battleStations;
    private Config config;

    private HeroManager hero;
    private StarManager star;

    private Set<Integer> dangerous;
    private long invisibleTime;

    public boolean SURE;

    Box current;

    private long waiting;

    private int DISTANCE_FROM_DANGEROUS;

    public CollectorModule() {
        dangerous = new HashSet<>();

        DISTANCE_FROM_DANGEROUS = 1500;
    }

    @Override
    public void install(Main main) {
        this.main = main;

        this.star = main.starManager;
        this.hero = main.hero;

        this.config = main.config;
        this.boxes = main.mapManager.boxes;
        this.ships = main.mapManager.ships;
        this.battleStations = main.mapManager.battleStations;
    }

    @Override
    public void tick() {

        if (isNotWaiting()) {

            checkInvisibility();
            checkDangerous();

            findBox();

            if (!tryCollectNearestBox() && (!hero.isMoving() || hero.isOutOfMap())) {
                hero.moveRandom();
            }
        }
    }

    boolean isNotWaiting() {
        return System.currentTimeMillis() > waiting;
    }

    boolean tryCollectNearestBox() {

        if (current != null) {
            collectBox();
            return true;
        }

        return false;
    }

    private void collectBox() {
        double distance = hero.location.distance(current);

        if (distance == 0 && SURE) {
            current.setCollected(true);
        } else if (distance < 100) {

            current.clickable.setRadius(200);
            hero.stop(true);
            hero.clickCenter();
            current.clickable.setRadius(0);

            if(!SURE) {
                current.setCollected(true);
            }

            waiting = System.currentTimeMillis() + current.boxInfo.waitTime + hero.timeTo(distance);

        } else {
            hero.move(current);
        }
    }

    private void checkDangerous() {
        if (config.STAY_AWAY_FROM_ENEMIES) {

            Location dangerous = closestDangerous();

            if (dangerous != null) stayAwayFromLocation(dangerous);
        }
    }

    private void checkInvisibility() {
        if (config.AUTO_CLOACK
                && !hero.invisible
                && System.currentTimeMillis() - invisibleTime > 60000
        ) {
            invisibleTime = System.currentTimeMillis();
            API.keyboardClick(config.AUTO_CLOACK_KEY);
        }
    }

    private void gotoRightMap() {
        main.setModule(new MapModule())
                .setTargetAndBack(star.fromId(config.WORKING_MAP), this);
    }

    private void stayAwayFromLocation(Location location) {
        double angle = location.angle(hero.location);
        double moveDistance = hero.shipInfo.speed;
        double distance = DISTANCE_FROM_DANGEROUS + 100;

        Location target = new Location(
                location.x - cos(angle) * distance,
                location.y - sin(angle) * distance
        );

        moveDistance = moveDistance - target.distance(hero);

        if (moveDistance > 0) {

            angle += moveDistance / 3000;

            target.x = location.x - cos(angle) * distance;
            target.y = location.y - sin(angle) * distance;
        }

        hero.move(target);
    }

    void findBox() {
        Location location = hero.location;
        double distance = 100_000;
        Box closest = null;

        for (Box box : boxes) {
            if (canCollect(box)) {
                double distanceCurrent = location.distance(box.location);
                if (distanceCurrent < distance) {
                    distance = distanceCurrent;
                    closest = box;
                }
            }
        }

        if (current == null || current.isCollected() || closest != null && isBetter(closest)) {
            current = closest;
        } else {
            current = null;
        }
    }

    private boolean canCollect(Box box) {
        return box.boxInfo.collect
                && !box.isCollected()
                && (!config.STAY_AWAY_FROM_ENEMIES || !isDangerousLocation(box.location));
    }

    private Location closestDangerous() {
        return hasEnemyBattleStationNear(hero.location) ?
                battleStations.get(0).location
                : findClosestEnemyAndAddToDangerousList();
    }

    private boolean isDangerousLocation(Location location) {
        return hasEnemyBattleStationNear(location) || hasEnemyNear(location);
    }

    private boolean hasEnemyBattleStationNear(Location location) {
        if (!battleStations.isEmpty()) {
            BattleStation station = battleStations.get(0);
            return station.info.isEnemy() && station.location.distance(location) < DISTANCE_FROM_DANGEROUS;
        }

        return false;
    }

    private boolean hasEnemyNear(Location location) {
        for (Ship ship : ships) {
            if (dangerous.contains(ship.id) && ship.location.distance(location) < DISTANCE_FROM_DANGEROUS) {
                return true;
            }
        }

        return false;
    }

    private Location findClosestEnemyAndAddToDangerousList() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy()
                    && !ship.invisible
                    && ship.location.distance(hero) < DISTANCE_FROM_DANGEROUS) {

                if (dangerous.contains(ship.id)) {
                    return ship.location;
                } else if (ship.isAttacking(hero)) {
                    dangerous.add(ship.id);
                    return ship.location;
                }

            }
        }

        return null;
    }

    private boolean isBetter(Box box) {

        double currentDistance = current.location.distance(hero);
        double newDistance = box.location.distance(hero);

        return currentDistance > 100 && currentDistance - 150 > newDistance;
    }

}
