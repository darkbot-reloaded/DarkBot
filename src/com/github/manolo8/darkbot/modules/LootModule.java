package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.utils.Drive;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.StrictMath.sin;

public class LootModule implements Module {

    private Main main;

    private List<Ship> ships;
    private List<Npc> npcs;

    private HeroManager hero;
    private Drive drive;

    private Config config;

    private long timer;

    public Npc target;
    private long laserTime;
    private long clickDelay;
    private boolean sab;

    private boolean locked;

    @Override
    public void install(Main main) {
        this.main = main;

        this.hero = main.hero;
        this.drive = main.hero.drive;

        this.ships = main.mapManager.entities.ships;
        this.npcs = main.mapManager.entities.npcs;
        this.config = main.config;
    }

    @Override
    public boolean canRefresh() {
        return target == null;
    }

    @Override
    public void tick() {

        if (checkDangerousAndCurrentMap()) {

            if (findTarget()) {
                moveToAnSafePosition();
                doKillTargetTick();
            } else if (!drive.isMoving()) {
                drive.moveRandom();
            }

        }

    }

    void doKillTargetTick() {
        if (main.mapManager.isTarget(target)) {

            if (main.mapManager.isCurrentTargetOwned()) {

                hero.attackMode();

                if (checkIfIsAttackingAndCanContinue()) {
                    checkSab();
                }

            } else {
                target.setTimerTo(90000);
                target = null;
            }

        } else {
            setTargetAndTryStartLaserAttack();
        }
    }

    boolean checkDangerousAndCurrentMap() {

        boolean mapWrong = config.WORKING_MAP != hero.map.id;
        boolean cont = target == null || (target.removed || target.health.hp == 0 || target.health.hp > 300000);
        boolean flee = config.RUN_FROM_ENEMIES && ((hero.health.hpPercent() < config.REPAIR_HP && cont) || hasEnemies());

        if (mapWrong || flee) {

            hero.runMode();

            if (mapWrong) {
                main.setModule(new MapModule())
                        .setTargetAndBack(main.starManager.fromId(main.config.WORKING_MAP));
            } else {
                Portal portal = main.starManager.next(hero.map, hero.location, hero.map);

                if (portal.location.distance(hero) < 100) {
                    if (System.currentTimeMillis() - timer > 10000 && isUnderAttack()) {
                        timer = System.currentTimeMillis();
                        API.keyboardClick('J');
                    }
                } else {
                    drive.move(portal);
                }

            }

            return false;
        }

        return true;
    }

    boolean findTarget() {
        if (target == null || target.removed) {
            target = closestNpc(hero.location.x, hero.location.y);
            locked = false;
        }

        return target != null;
    }

    private void checkSab() {
        if (config.AUTO_SAB && hero.health.shieldPercent() < 0.6 && target.health.shield > 12000) {

            if (!sab) {
                API.keyboardClick(config.AUTO_SAB_KEY);
                sab = true;
            }

        } else if (sab) {
            API.keyboardClick(config.AMMO_KEY);
            sab = false;
        }
    }

    private boolean checkIfIsAttackingAndCanContinue() {

        long laser = System.currentTimeMillis() - laserTime;

        boolean attacking = hero.isAttacking(target);
        boolean bugged = (!target.health.isDecreasedIn(1000) && laser > 1000);

        if ((!attacking || bugged) && hero.location.distance(target) < 800 && laser > 1500) {
            setRadiusAndClick(2);
            laserTime = System.currentTimeMillis();
        }

        return true;
    }

    private void setTargetAndTryStartLaserAttack() {
        if (hero.location.distance(target) <= target.npcInfo.radius && System.currentTimeMillis() - clickDelay > 1000) {

            hero.setTarget(target);

            setRadiusAndClick(1);
            API.keyboardClick(config.AMMO_KEY);
            clickDelay = System.currentTimeMillis();
            locked = true;

        } else if (!locked) {
            target = null;
        }

    }

    private void setRadiusAndClick(int times) {
        target.clickable.setRadius(800);

        drive.clickCenter(times);

        target.clickable.setRadius(0);
    }

    void moveToAnSafePosition() {

        Location locationCurrent = target.location;
        Location locationHero = hero.location;

        double distance = locationHero.distance(locationCurrent);
        double radius = target.npcInfo.radius;

        boolean approaching = locationHero.distance(locationCurrent) < locationHero.distance(locationCurrent.lastX, locationCurrent.lastY);
        double speed = min(200, locationCurrent.measureSpeed()) * 0.625;
        boolean closest = distance <= radius + (approaching ? speed : -speed);

        double moveDistance = hero.shipInfo.speed * 0.625 + speed;
        double centerDistance = radius;
        double angle = locationCurrent.angle(locationHero);
        boolean first = target.health.hpPercent() == 1;

        if (first)
            centerDistance *= 0.75;
        else if (!closest)
            centerDistance = centerDistance * 0.70 - speed;
        else if (approaching)
            centerDistance += speed;
        else
            centerDistance -= speed;

        double x = locationCurrent.x - cos(angle) * centerDistance;
        double y = locationCurrent.y - sin(angle) * centerDistance;

        boolean circle = !first && closest && (moveDistance = (moveDistance - (locationHero.distance(x, y)))) > 0;

        if (circle) {
            double add = moveDistance / centerDistance;

            angle += add;

            x = locationCurrent.x - cos(angle) * centerDistance;
            y = locationCurrent.y - sin(angle) * centerDistance;
        }

        drive.move(x, y);
    }

    private boolean isUnderAttack() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy()) {

                if (ship.isAttacking(hero)) {
                    return true;
                }

            }
        }
        return false;
    }

    private boolean hasEnemies() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy()) {

                if (ship.isInTimer()) {
                    return true;
                } else if (ship.isAttacking(hero)) {
                    ship.setTimerTo(400_000);
                    return true;
                }

            }
        }
        return false;
    }

    private boolean isAttackedByOthers(Npc npc) {

        if (npc.isInTimer()) return true;

        for (Ship ship : ships) {
            if (ship.isAttacking(npc)) {
                npc.setTimerTo(90000);
                return true;
            }
        }

        return false;
    }

    private Npc closestNpc(double x, double y) {
        double distance = 40000;
        int priority = 0;

        Npc maxPriority = null;
        Npc minDistance = null;

        for (Npc npc : npcs) {

            if (npc.npcInfo.kill && !isAttackedByOthers(npc)) {

                double distanceCurrent = npc.location.distance(x, y);
                int priorityCurrent = npc.npcInfo.priority;

                if (distanceCurrent < distance) {
                    distance = distanceCurrent;
                    minDistance = npc;
                }

                if (priorityCurrent >= priority) {
                    priority = priorityCurrent;
                    maxPriority = npc;
                }

            }
        }

        return minDistance == null ? null : priority > minDistance.npcInfo.priority ? maxPriority : minDistance;
    }
}
