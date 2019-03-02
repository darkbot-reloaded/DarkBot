package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.utils.Time;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.Math.random;
import static java.lang.Math.sin;

public class EventModule implements Module {

    private static final double TAU = Math.PI * 2;

    private Main main;
    private Config config;
    private List<Npc> npcs;
    private List<Box> boxes;
    private HeroManager hero;
    private Drive drive;
    private Location direction;

    public Npc target;
    private boolean shooting;
    private long clickDelay;
    private long lastNpc = System.currentTimeMillis();
    private long timeSinceNpc;

    private Box drop;
    private long waiting;

    private int clickReward = -1;

    @Override
    public void install(Main main) {
        this.main = main;
        this.config = main.config;

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.npcs = main.mapManager.entities.npcs;
        this.boxes = main.mapManager.entities.boxes;
    }

    @Override
    public boolean canRefresh() {
        return timeSinceNpc > 2 * 60 * 1000 && (timeSinceNpc <  15 * 60 * 1000 || timeSinceNpc > 25 * 60 * 1000);
    }

    @Override
    public String status() {
        timeSinceNpc = System.currentTimeMillis() - this.lastNpc;
        return timeSinceNpc > 1000 ? "Waiting: " + Time.toString(timeSinceNpc) : null;
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() < waiting) return;

        if (findTarget()) {
            hero.attackMode();
            lastNpc = System.currentTimeMillis();
            setTargetAndTryStartLaserAttack();
            moveToAnSafePosition();
        } else if (findBox()) {
            hero.runMode();
            pickUpBox();
            if (config.EVENT.PROGRESS) clickReward = 2;
            return;
        } else if (clickReward == -1 && timeSinceNpc > 5000 && !hero.locationInfo.isMoving() &&
                hero.locationInfo.distance(MapManager.internalWidth / 2d, MapManager.internalHeight / 2d) > 600) {
            hero.runMode();
            hero.drive.move(MapManager.internalWidth / 2d + (random() * 400 - 200),
                    MapManager.internalHeight / 2d + (random() * 400 - 200));
        } else if (timeSinceNpc > 10000 && !hero.drive.isMoving()) hero.attackMode();

        main.guiManager.pet.setEnabled(timeSinceNpc < 3000);
        if (clickReward > -1 && main.guiManager.eventProgress.show(clickReward > 0)) {
            if (clickReward > 0) main.guiManager.eventProgress.click(200, 335);
            waiting = System.currentTimeMillis() + 500;
            clickReward--;
        }
    }

    private boolean findTarget() {
        if (target == null || target.removed) target = npcs.isEmpty() ? null : npcs.get(0);
        return target != null;
    }

    private void setTargetAndTryStartLaserAttack() {
        boolean locked = main.mapManager.isTarget(target);
        double distance = hero.locationInfo.distance(target);
        if (locked && !shooting) {
            if (distance > 550) return;
            API.keyboardClick(config.AMMO_KEY);
            shooting = true;
            if (target.health.maxHp > 0) API.keyboardClick(config.EVENT.SHIP_ABILITY);
            return;
        }
        if (locked) return;

        if (hero.locationInfo.distance(target) < 750 && System.currentTimeMillis() - clickDelay > 1000) {
            hero.setTarget(target);
            setRadiusAndClick();
            clickDelay = System.currentTimeMillis();

            shooting = false;
        }
    }

    private void setRadiusAndClick() {
        target.clickable.setRadius(800);
        drive.clickCenter(1);
        target.clickable.setRadius(0);
    }

    private void moveToAnSafePosition() {
        if (!hero.drive.isMoving()) direction = null;
        Location heroLoc = hero.locationInfo.now;
        if (target == null || target.locationInfo == null) return;
        Location targetLoc = target.locationInfo.destinationInTime(400);

        double angle = targetLoc.angle(heroLoc), distance = heroLoc.distance(targetLoc),
                angleDiff = Math.abs(target.locationInfo.angle - heroLoc.angle(target.locationInfo.now)) % TAU;
        if (angleDiff > Math.PI) angleDiff = TAU - angleDiff;

        boolean npcFollowing = target.locationInfo.isMoving() && (angleDiff < 1.25);

        if (target == hero.target && shooting && (hero.health.hpPercent() + hero.health.shieldPercent() < 0.8 ||
                hero.health.isDecreasedIn(2000) || (hero.health.isDecreasedIn(60000) && npcFollowing))) {
            distance = 800 - (hero.health.shieldPercent() * 300);
            angle += 0.2 + (random() * 0.1);
        } else {
            if (distance <= 320 || (direction != null && targetLoc.distance(direction) <= 320)) return;
            distance = 200 + Math.random() * 75;
            angle += random() * 2 - 1;
        }

        do {
            direction = new Location(targetLoc.x - cos(angle) * distance, targetLoc.y - sin(angle) * distance);
            angle += 0.3;
            distance += 2;
        } while (main.mapManager.isOutOfMap(direction.x, direction.y));

        drive.move(direction.x, direction.y);
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
            drop.setCollected(true);

            drive.stop(false);
            drop.clickable.setRadius(1200);
            drive.clickCenter(1);
            drop.clickable.setRadius(0);

            waiting = System.currentTimeMillis() + hero.timeTo(distance) + 30 + 750;
        } else {
            drive.move(drop);
        }
    }

}
