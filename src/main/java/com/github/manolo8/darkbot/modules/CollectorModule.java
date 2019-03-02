package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.Comparator;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

public class CollectorModule implements Module {

    private Main main;

    private List<Box> boxes;
    private List<Ship> ships;
    private Config config;

    private HeroManager hero;
    private Drive drive;

    private long invisibleTime;

    Box current;

    private long waiting;

    private int DISTANCE_FROM_DANGEROUS;

    public CollectorModule() {
        DISTANCE_FROM_DANGEROUS = 1500;
    }

    @Override
    public void install(Main main) {
        this.main = main;

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.config = main.config;
        this.boxes = main.mapManager.entities.boxes;
        this.ships = main.mapManager.entities.ships;
    }

    @Override
    public boolean canRefresh() {
        return isNotWaiting();
    }

    @Override
    public void tick() {

        if (isNotWaiting() && checkCurrentMap()) {

            checkInvisibility();
            checkDangerous();

            findBox();

            if (!tryCollectNearestBox() && (!drive.isMoving() || drive.isOutOfMap())) {
                drive.moveRandom();
            }
        }
    }


    private boolean checkCurrentMap() {
        boolean mapWrong = config.WORKING_MAP != hero.map.id;

        if (mapWrong) {

            hero.runMode();

            main.setModule(new MapModule())
                    .setTargetAndBack(main.starManager.fromId(main.config.WORKING_MAP));

            return false;
        }

        return true;
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
        double distance = hero.locationInfo.distance(current);

        if (distance < 100) {
            drive.stop(false);
            current.clickable.setRadius(800);
            drive.clickCenter(1);
            current.clickable.setRadius(0);

            current.setCollected(true);

            waiting = System.currentTimeMillis() + current.boxInfo.waitTime + hero.timeTo(distance) + 30;

        } else {
            drive.move(current);
        }
    }

    private void checkDangerous() {
        if (config.STAY_AWAY_FROM_ENEMIES) {

            Location dangerous = findClosestEnemyAndAddToDangerousList();

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

    private void stayAwayFromLocation(Location awayLocation) {

        Location heroLocation = hero.locationInfo.now;

        double angle = awayLocation.angle(heroLocation);
        double moveDistance = hero.shipInfo.speed;
        double distance = DISTANCE_FROM_DANGEROUS + 100;

        Location target = new Location(
                awayLocation.x - cos(angle) * distance,
                awayLocation.y - sin(angle) * distance
        );

        moveDistance = moveDistance - target.distance(heroLocation);

        if (moveDistance > 0) {

            angle += moveDistance / 3000;

            target.x = awayLocation.x - cos(angle) * distance;
            target.y = awayLocation.y - sin(angle) * distance;
        }

        drive.move(target);
    }

    void findBox() {
        LocationInfo locationInfo = hero.locationInfo;

        Box best = boxes.stream()
                .filter(this::canCollect)
                .min(Comparator.comparingDouble(locationInfo::distance)).orElse(null);
        this.current = current == null || best == null || current.isCollected() || isBetter(best) ? best : current;
    }

    private boolean canCollect(Box box) {
        return box.boxInfo.collect
                && !box.isCollected()
                && (drive.canMove(box.locationInfo.now));
    }

    private Location findClosestEnemyAndAddToDangerousList() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy()
                    && !ship.invisible
                    && ship.locationInfo.distance(hero) < DISTANCE_FROM_DANGEROUS) {

                if (ship.isInTimer()) {
                    return ship.locationInfo.now;
                } else if (ship.isAttacking(hero)) {
                    ship.setTimerTo(400_000);
                    return ship.locationInfo.now;
                }

            }
        }

        return null;
    }

    private boolean isBetter(Box box) {

        double currentDistance = current.locationInfo.distance(hero);
        double newDistance = box.locationInfo.distance(hero);

        return currentDistance > 100 && currentDistance - 150 > newDistance;
    }

}
