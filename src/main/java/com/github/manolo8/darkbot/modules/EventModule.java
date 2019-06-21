package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.manager.PetManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import com.github.manolo8.darkbot.utils.MathUtils;
import com.github.manolo8.darkbot.utils.Time;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.lang.Math.random;

public class EventModule implements Module {

    private static final String MAP_PREFIX = "Experiment Zone 2-";
    private static int TIME_PER_NPC = 24 * 60 * 1000,
            TIME_TO_NEXT_NPC = 20 * 60 * 1000,
            TRAVEL_TIME = 3 * 60 * 1000;

    private Main main;
    private Config config;
    private List<Npc> npcs;
    private List<Box> boxes;
    private HeroManager hero;
    private Drive drive;
    private PetManager pet;

    private NpcAttacker attack;

    private static TreeMap<Integer, MapTiming> maps = new TreeMap<>();
    class MapTiming {
        private Map map;
        private Long npcTime;

        MapTiming(Map map) {
            this.map = map;
        }

        private Integer sinceNpc() {
            return npcTime == null ? null : (int) (System.currentTimeMillis() - npcTime);
        }
        private Integer nextNpc() {
            Integer since = sinceNpc();
            if (since == null) return null;
            int time = TIME_TO_NEXT_NPC - (since % TIME_PER_NPC);
            if (time < 0) time += TIME_PER_NPC;
            return time;
        }
    }
    private Integer sinceNpc() {
        return maps.getOrDefault(hero.map.id, new MapTiming(null)).sinceNpc();
    }

    private Box drop;
    private long waiting;

    private int clickReward = -1;

    @Override
    public void install(Main main) {
        this.main = main;
        this.attack = new NpcAttacker(main);
        this.config = main.config;

        this.hero = main.hero;
        this.drive = main.hero.drive;
        this.pet = main.guiManager.pet;

        this.npcs = main.mapManager.entities.npcs;
        this.boxes = main.mapManager.entities.boxes;

        main.starManager.mapSet()
                .filter(m -> m.name.startsWith(MAP_PREFIX))
                .forEach(m -> maps.putIfAbsent(m.id, new MapTiming(m)));
    }


    @Override
    public boolean canRefresh() {
        Integer sinceNpc = sinceNpc();
        return sinceNpc != null && (sinceNpc > 2 * 60 * 1000 && (sinceNpc <  15 * 60 * 1000 || sinceNpc > 25 * 60 * 1000));
    }

    @Override
    public String status() {
        return (attack.hasTarget() ? attack.status() + " - " : drop != null ? "Collecting drop - " :
                clickReward > -1 ? "Clicking reward - " : "") + "Waiting: [ " +
                maps.values().stream()
                        .filter(mt -> config.EVENT.MAP_SWITCHING ? mt.map.name.startsWith(MAP_PREFIX) : mt.map == hero.map)
                        .map(MapTiming::sinceNpc)
                        .map(Time::toString).collect(Collectors.joining(" | ")) + " ]";
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() < waiting) return;
        MapTiming mt = maps.computeIfAbsent(hero.map.id, id -> new MapTiming(hero.map));

        int since = Optional.ofNullable(sinceNpc()).orElse(60_000);

        if (config.EVENT.MAP_SWITCHING && (!hero.map.name.startsWith(MAP_PREFIX) ||
                (sinceNpc() != null && since > 3000 && since < 20000))) {
            if (clickReward != -1) {
                if (!hero.locationInfo.isMoving()) {
                    Portal next = main.starManager.next(hero, hero.map);
                    if (next != null) drive.move(next);
                }
            } else {
                MapTiming next = maps.values().stream()
                        .filter(timing -> timing.map.name.startsWith(MAP_PREFIX))
                        .min(Comparator.comparing(MapTiming::nextNpc, Comparator.nullsFirst(
                                Comparator.comparingInt((Integer t) -> {
                                    t -= TRAVEL_TIME;
                                    return t > 0 ? t : t + TIME_PER_NPC;
                                })))).orElse(mt);
                if (next != mt) {
                    main.setModule(new MapModule()).setTarget(next.map);
                    return;
                }
            }
        }

        if (findTarget()) {
            mt.npcTime = System.currentTimeMillis();
            pet.setEnabled(true);
            hero.attackMode();

            moveToAnSafePosition();
            attack.doKillTargetTick();
        } else if (findBox()) {
            hero.runMode();
            pickUpBox();
            if (config.EVENT.PROGRESS) clickReward = 2;
            return;
        } else if (clickReward == -1 && since > 5000 && !hero.locationInfo.isMoving() &&
                hero.locationInfo.distance(MapManager.internalWidth / 2d, MapManager.internalHeight / 2d) > 800) {
            hero.runMode();
            hero.drive.move(MapManager.internalWidth / 2d + (random() * 600 - 300),
                    MapManager.internalHeight / 2d + (random() * 600 - 300));
        } else if (since > 10000 && !hero.drive.isMoving()) hero.attackMode();

        pet.setEnabled(since < 3000 && pet.isEnabled());
        if (clickReward > -1 && main.guiManager.eventProgress.show(clickReward > 0)) {
            if (clickReward > 0) main.guiManager.eventProgress.click(200, 335);
            waiting = System.currentTimeMillis() + 500;
            clickReward--;
        }
    }

    private boolean findTarget() {
        if (!attack.hasTarget()) attack.target = npcs.isEmpty() ? null : npcs.get(0);
        return attack.hasTarget();
    }

    private void moveToAnSafePosition() {
        Location direction = drive.movingTo();
        Location heroLoc = hero.locationInfo.now;
        Location targetLoc = attack.target.locationInfo.destinationInTime(400);

        double angle = targetLoc.angle(heroLoc), distance = heroLoc.distance(targetLoc),
                angleDiff = MathUtils.angleDiff(attack.target.locationInfo.angle, heroLoc.angle(attack.target.locationInfo.now));

        boolean npcFollowing = attack.target.locationInfo.isMoving() && (angleDiff < 1.5);

        if (attack.target == hero.target && !attack.castingAbility() &&
                (hero.health.hpPercent() + hero.health.shieldPercent() < 0.8 ||
                        hero.health.hpDecreasedIn(2000) || (hero.health.hpDecreasedIn(60000) && npcFollowing))) {
            distance = 800 - (hero.health.shieldPercent() * 300);
            angle += 0.15 + (random() * 0.1);
        } else {
            if (distance <= 320 || (direction != null && targetLoc.distance(direction) <= 320)) return;
            distance = 200 + Math.random() * 75;
            angle += random() * 2 - 1;
        }
        direction = Location.of(targetLoc, angle, distance);

        while (!drive.canMove(direction) && distance < 10000)
            direction.toAngle(targetLoc, angle += 0.3, distance += 2);
        if (distance >= 10000) direction.toAngle(targetLoc, angle, 600);

        drive.move(direction);
    }

    private boolean findBox() {
        if (drop == null || drop.removed) {
            drop = boxes.stream().filter(b -> !b.removed && !b.isCollected()
                    && b.type.contains("BEACON")).findAny().orElse(null);
        }
        return drop != null;
    }

    private void pickUpBox() {
        double distance = hero.locationInfo.distance(drop);

        if (distance < 300) {
            drop.setCollected();

            drive.stop(false);
            drop.clickable.setRadius(800);
            drive.clickCenter(true, drop.locationInfo.now);
            drop.clickable.setRadius(0);

            waiting = System.currentTimeMillis() + hero.timeTo(distance) + 30 + 750;
        } else {
            drive.move(drop);
        }
    }

}
