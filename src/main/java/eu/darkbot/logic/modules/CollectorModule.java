package eu.darkbot.logic.modules;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.Box;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.managers.*;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.plugin.Feature;
import eu.darkbot.api.plugin.Inject;
import eu.darkbot.api.plugin.Module;
import eu.darkbot.config.ConfigAPI;
import eu.darkbot.logic.SafetyFinder;

import java.util.Collection;
import java.util.Comparator;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

@Feature(name = "Collector", description = "Resource-only collector module. Can cloak.")
public class CollectorModule implements Module {

    protected static final int DISTANCE_FROM_DANGEROUS = 1500;

    protected final PetAPI pet;
    protected final HeroAPI hero;
    protected final StarAPI star;
    protected final StatsAPI stats;
    protected final ConfigAPI config;
    protected final PluginAPI pluginAPI;
    protected final MovementAPI movement;

    protected final SafetyFinder safetyFinder;

    protected final Collection<Box> boxes;
    protected final Collection<Ship> ships;
    protected final Collection<Portal> portals;

    public Box currentBox;
    protected long refreshing;
    private long invisibleUntil, waitingUntil;

    @Inject
    public CollectorModule(PetAPI pet,
                           HeroAPI hero,
                           StarAPI star,
                           StatsAPI stats,
                           ConfigAPI config,
                           PluginAPI pluginAPI,
                           MovementAPI movement,
                           EntitiesAPI entities) {
        this.pet = pet;
        this.hero = hero;
        this.star = star;
        this.stats = stats;
        this.config = config;
        this.pluginAPI = pluginAPI;
        this.movement = movement;

        this.boxes = entities.getEntities(Box.class);
        this.ships = entities.getEntities(Ship.class);
        this.portals = entities.getEntities(Portal.class);

        this.safetyFinder = new SafetyFinder(pluginAPI); //TODO replace lazy with weak listener in safety
    }

    @Override
    public boolean canRefresh() {
        return isNotWaiting();
    }

    @Override
    public String getStatus() {
        if (currentBox == null) return "Roaming";

        return currentBox.isCollected() ?
                "Collecting " + currentBox.getTypeName() + " " + (waitingUntil - System.currentTimeMillis()) + "ms"
                : "Moving to " + currentBox.getTypeName();
    }

    @Override
    public void onTickModule() {
        if (isNotWaiting() && checkDangerousAndCurrentMap()) {
            pet.setEnabled(true);
            checkInvisibility();
            checkDangerous();

            findBox();

            if (!tryCollectNearestBox() && (!movement.isMoving() || movement.isOutOfMap())) {
                movement.moveRandom();
            }
        }
    }

    protected boolean isNotWaiting() {
        return System.currentTimeMillis() > waitingUntil ||
                currentBox == null ||
                currentBox.isRemoved();
    }

    protected boolean checkDangerousAndCurrentMap() {
        safetyFinder.setRefreshing(System.currentTimeMillis() <= refreshing);
        return safetyFinder.tick() && checkMap();
    }

    protected boolean checkMap() {
        if (this.config.GENERAL.WORKING_MAP != this.star.getCurrentMap().getId() && !portals.isEmpty()) {
            this.pluginAPI.setModule(new MapModule(pluginAPI, star.getOrCreateMapById(config.GENERAL.WORKING_MAP)));
            return false;
        }
        return true;
    }

    public void findBox() {
        Location heroLoc = hero.getLocationInfo();

        Box best = boxes
                .stream()
                .filter(this::canCollect)
                .min(Comparator.<Box>comparingInt(b -> b.getInfo().getPriority())
                        .thenComparingDouble(heroLoc::distanceTo)).orElse(null);

        this.currentBox = currentBox == null ||
                best == null ||
                currentBox.isCollected() ||
                isBetter(best) ?
                best : currentBox;
    }

    protected boolean canCollect(Box box) {
        return box.getInfo().shouldCollect()
                && !box.isCollected()
                && movement.canMove(box)
                && (!box.getTypeName().equals("FROM_SHIP") || stats.getCargo() < stats.getMaxCargo())
                && !isContested(box);
    }

    public boolean tryCollectNearestBox() {

        if (currentBox != null) {
            collectBox();
            return true;
        }

        return false;
    }

    protected void collectBox() {
        double distance = hero.getLocationInfo().distanceTo(currentBox);

        if (distance < 200) {
            movement.stop(false);

            if (currentBox.tryCollect())
                currentBox.setCollected();

            waitingUntil = System.currentTimeMillis()
                    + currentBox.getInfo().getWaitTime()
                    + Math.min(1_000, currentBox.getRetries() * 100) // Add 100ms per retry, max 1 second
                    + hero.timeTo(distance) + 30;
        } else {
            movement.moveTo(currentBox);
        }
    }

    protected void checkDangerous() {
        if (config.COLLECT.STAY_AWAY_FROM_ENEMIES) {

            Location dangerous = findClosestEnemyAndAddToDangerousList();

            if (dangerous != null) stayAwayFromLocation(dangerous);
        }
    }

    public void checkInvisibility() {
        if (config.COLLECT.AUTO_CLOACK
                && !hero.isInvisible()
                && System.currentTimeMillis() - invisibleUntil > 60000) {
            invisibleUntil = System.currentTimeMillis();
            API.keyboardClick(config.COLLECT.AUTO_CLOACK_KEY);
        }
    }

    protected void stayAwayFromLocation(Location awayLocation) {

        Location heroLocation = hero.getLocationInfo();

        double angle = awayLocation.angleTo(heroLocation);
        double moveDistance = hero.getSpeed();
        double distance = DISTANCE_FROM_DANGEROUS + 100;

        Location target = Location.of(awayLocation, angle, distance);

        moveDistance = moveDistance - target.distanceTo(heroLocation);

        if (moveDistance > 0) {

            angle += moveDistance / 3000;
            target.setTo(awayLocation.getX() - cos(angle) * distance,
                    awayLocation.getY() - sin(angle) * distance);

        }

        movement.moveTo(target);
    }

    protected Location findClosestEnemyAndAddToDangerousList() {
        for (Ship ship : ships) {
            if (ship.isEnemy()
                    && !ship.isInvisible()
                    && ship.getLocationInfo().distanceTo(hero) < DISTANCE_FROM_DANGEROUS) {

                if (ship instanceof com.github.manolo8.darkbot.core.entities.Ship) {
                    com.github.manolo8.darkbot.core.entities.Ship ship1 = (com.github.manolo8.darkbot.core.entities.Ship) ship;

                    if (ship1.isInTimer()) {
                        return ship.getLocationInfo();
                    } else if (ship.isAttacking(hero)) {
                        ship1.setTimerTo(config.GENERAL.RUNNING.REMEMBER_ENEMIES_FOR * 1000L);
                        return ship.getLocationInfo();
                    }
                }
            }
        }

        return null;
    }

    private boolean isBetter(Box box) {

        double currentDistance = currentBox.getLocationInfo().distanceTo(hero);
        double newDistance = box.getLocationInfo().distanceTo(hero);

        return currentDistance > 100 && currentDistance - 150 > newDistance;
    }

    protected boolean isContested(Box box) {
        if (!config.COLLECT.IGNORE_CONTESTED_BOXES) return false;

        double heroTimeTo = hero.timeTo(box);
        return ships.stream()
                .filter(ship -> ship.getDestination().isPresent())
                .filter(ship -> ship.getDestination().get().distanceTo(box) == 0)
                .anyMatch(ship -> heroTimeTo < ship.timeTo(box));
    }
}
