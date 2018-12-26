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
import com.github.manolo8.darkbot.core.utils.pet.PetLoot;

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
    private Box current;

    private int DISTANCE_FROM_DANGEROUS;

    private Set<String> collectibleBoxes;

    public CollectorModule() {
        dangerous = new HashSet<>();
        collectibleBoxes = new HashSet<>();

        collectibleBoxes.add("BONUS_BOX");
        collectibleBoxes.add("GIFT_BOXES");

        DISTANCE_FROM_DANGEROUS = 1500;
    }

    @Override
    public void install(Main main) {
        main.guiManager.module = new PetLoot();
        main.guiManager.nullPetModuleOnActivate = false;

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

        if (star.fromId(config.WORKING_MAP) != hero.map) {
            gotoRightMap();
            return;
        }

        checkInvisibility();
        checkDangerous();

        tryCollectNearestBoxOrRandomMove();
    }

    private void tryCollectNearestBoxOrRandomMove() {
        Box box = findNearestBoxSafe(hero.location);

        if (current == null || box == null || current.isCollected() || isBetter(box)) {
            current = box;
        }

        if (current != null) {
            collectBox();
        } else if (!main.hero.isMoving() || main.hero.isOutOfMap()) {
            hero.moveRandom();
        }
    }

    private void collectBox() {
        hero.move(current);

        double distance = hero.location.distance(current);

        if (distance < 100) {
            current.clickable.setRadius(200);
            hero.stop(false);
            hero.clickCenter();
            current.clickable.setRadius(0);

            current.setCollected(true);
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

    private Box findNearestBoxSafe(Location location) {
        double distance = 40000;
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

        return closest;
    }

    private boolean canCollect(Box box) {
        return collectibleBoxes.contains(box.type)
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

        //if currentDistance < 100, box already marked as collected!

        return currentDistance > 100 && currentDistance - 150 > newDistance;
    }

}
