package eu.darkbot.logic.modules;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.entities.Box;
import eu.darkbot.api.entities.Entity;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.Ship;
import eu.darkbot.api.entities.other.Effect;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.managers.MovementAPI;
import eu.darkbot.api.managers.PetAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import eu.darkbot.api.managers.StatsAPI;
import eu.darkbot.api.objects.Location;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.config.ConfigAPI;
import eu.darkbot.logic.SafetyFinder;
import eu.darkbot.utils.Time;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

@Feature(name = "Collector", description = "Resource-only collector module. Can cloak.")
public class CollectorModule implements Module {

    protected static final String CLOAK_ITEM_ID = "equipment_extra_cpu_cl04k";
    protected static final int DISTANCE_FROM_DANGEROUS = 1500;

    protected final BotAPI bot;
    protected final PetAPI pet;
    protected final HeroAPI hero;
    protected final StarSystemAPI star;
    protected final StatsAPI stats;
    protected final ConfigAPI config;
    protected final PluginAPI pluginAPI;
    protected final MovementAPI movement;
    protected final HeroItemsAPI heroItems;

    protected final SafetyFinder safetyFinder;

    protected final Collection<Box> boxes;
    protected final Collection<Ship> ships;
    protected final Collection<Portal> portals;

    public Box currentBox;
    protected long refreshing;

    private long invisibleUntil, waitingUntil;

    //@Feature.Inject
    public CollectorModule(BotAPI bot,
                           PetAPI pet,
                           HeroAPI hero,
                           StarSystemAPI star,
                           StatsAPI stats,
                           ConfigAPI config,
                           PluginAPI pluginAPI,
                           MovementAPI movement,
                           HeroItemsAPI heroItems,
                           EntitiesAPI entities,
                           SafetyFinder safetyFinder) {
        this.bot = bot;
        this.pet = pet;
        this.hero = hero;
        this.star = star;
        this.stats = stats;
        this.config = config;
        this.pluginAPI = pluginAPI;
        this.heroItems = heroItems;
        this.movement = movement;

        this.boxes = entities.getBoxes();
        this.ships = entities.getPlayers();
        this.portals = entities.getPortals();

        this.safetyFinder = safetyFinder;
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
                !currentBox.isValid();
    }

    protected boolean checkDangerousAndCurrentMap() {
        safetyFinder.setRefreshing(System.currentTimeMillis() <= refreshing);
        return safetyFinder.tick() && checkMap();
    }

    protected boolean checkMap() {
        if (!portals.isEmpty() && config.GENERAL.WORKING_MAP != star.getCurrentMap().getId()) {
            this.bot.setModule(pluginAPI.createInstance(MapModule.class))
                    .setTarget(star.getOrCreateMapById(config.GENERAL.WORKING_MAP));
            return false;
        }

        return true;
    }

    public void findBox() {
        Box best = boxes
                .stream()
                .filter(this::canCollect)
                .min(Comparator.<Box>comparingInt(b -> b.getInfo().getPriority())
                        .thenComparingDouble(hero.getLocationInfo()::distanceTo)).orElse(null);

        this.currentBox = currentBox == null || best == null || currentBox.isCollected() || isBetter(best)
                ? best : currentBox;
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
            //movement.stop(false);
            if (!hero.hasEffect(Effect.BOX_COLLECTING)
                    || hero.getLocationInfo().distanceTo(currentBox) == 0)
                currentBox.tryCollect();
            else return;

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

            heroItems.findItem(HeroItemsAPI.Category.CPUS, CLOAK_ITEM_ID)
                    .filter(heroItems::isSelectable)
                    .filter(cloak -> cloak.getQuantity() > 0)
                    .ifPresent(heroItems::selectItem);
        }
    }

    protected void stayAwayFromLocation(Location awayLocation) {
        double angle = awayLocation.angleTo(hero);
        double moveDistance = hero.getSpeed();
        double distance = DISTANCE_FROM_DANGEROUS + 100;

        Location target = Location.of(awayLocation, angle, distance);
        moveDistance = moveDistance - target.distanceTo(hero);

        if (moveDistance > 0) {
            angle += moveDistance / 3000;
            target.setTo(awayLocation.getX() - cos(angle) * distance,
                    awayLocation.getY() - sin(angle) * distance);
        }

        movement.moveTo(target);
    }

    protected Location findClosestEnemyAndAddToDangerousList() {
        return ships.stream()
                .filter(s -> s.getEntityInfo().isEnemy() && !s.isInvisible() && s.distanceTo(hero) < DISTANCE_FROM_DANGEROUS)
                .peek(s -> {
                    if (!s.isBlacklisted() && s.isAttacking(hero))
                        s.setBlacklisted(config.GENERAL.RUNNING.REMEMBER_ENEMIES_FOR * 1000L);
                })
                .map(Ship::getLocationInfo)
                .min(Comparator.comparingDouble(location -> location.distanceTo(hero)))
                .orElse(null);
    }

    private boolean isBetter(Box box) {
        double currentDistance = currentBox.getLocationInfo().distanceTo(hero);
        return currentDistance > 100 && currentDistance - 150 > box.getLocationInfo().distanceTo(hero);
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
