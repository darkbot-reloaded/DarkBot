package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Npc;
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
import static java.lang.Math.min;
import static java.lang.StrictMath.sin;

public class LootModule implements Module {

    private Main main;

    private List<Ship> ships;
    private List<Npc> npcs;

    private StarManager star;
    private HeroManager hero;

    private Config config;

    private Npc current;
    private long laserTime;
    private long clickDelay;
    private boolean sab;

    private Set<Integer> ignore;
    private Set<Integer> dangerous;

    public LootModule() {
        dangerous = new HashSet<>();
        ignore = new HashSet<>();
    }

    @Override
    public void install(Main main) {
        main.guiManager.module = null;
        main.guiManager.nullPetModuleOnActivate = false;

        this.main = main;

        this.star = main.starManager;
        this.hero = main.hero;

        this.ships = main.mapManager.ships;
        this.npcs = main.mapManager.npcs;
        this.config = main.config;
    }

    @Override
    public void tick() {

        if (checkDangerousAndCurrentMap()) {

            findNearestTarget();

            if (current != null) {

                moveToAnSafePosition();

                if (hero.isTarget(current)) {

                    hero.attackMode();

                    if (checkIfIsAttackingAndCanContinue()) {
                        checkSab();
                    }

                } else {
                    setTargetAndTryStartLaserAttack();
                }

            } else if (!hero.isMoving()) {
                hero.moveRandom();

                if (ignore.size() > 10) {
                    ignore.clear();
                }

            }

        }

    }

    private boolean checkDangerousAndCurrentMap() {
        boolean mapWrong = star.fromId(config.WORKING_MAP) != hero.map;
        boolean cont = current == null || (current.removed || current.health.hp == 0 || current.health.hp > 300000);
        boolean flee = config.RUN_FROM_ENEMIES && ((hero.health.hpPercent() < 0.5 && cont) || hasEnemies());

        if (mapWrong || flee) {

            hero.runMode();

            if (mapWrong || hero.health.isDecreasedIn(3000)) {
                main.setModule(new MapModule())
                        .setTargetAndBack(main.starManager.fromId(main.config.WORKING_MAP), this);
            }

            return false;
        }

        return true;
    }

    private void findNearestTarget() {
        if (current == null || current.isInvalid()) {
            current = closestNpc(hero.location.x, hero.location.y);
        }
    }

    private void checkSab() {
        if (config.AUTO_SAB && hero.health.shieldPercent() < 0.6 && current.health.shield > 12000) {

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

        boolean attacking = hero.isAttacking(current);
        boolean bugged = (!current.health.isDecreasedIn(12000) && laser > 12000);

        if ((!attacking || bugged) && hero.location.distance(current) < 800 && laser > 500) {

            if (isAttackedByOthers(current)) {
                current = null;
                return false;
            } else {

                API.keyboardClick('Z');
                laserTime = System.currentTimeMillis();

                return true;
            }

        }

        return true;
    }

    private void setTargetAndTryStartLaserAttack() {
        hero.setTarget(current);

        if (hero.location.distance(current) < 800 && System.currentTimeMillis() - clickDelay > 500) {
            current.clickable.setRadius(800);

            hero.clickCenter();

            current.clickable.setRadius(0);
            API.keyboardClick('Z');

            clickDelay = System.currentTimeMillis();
        }
    }

    private void moveToAnSafePosition() {

        Location locationCurrent = current.location;
        Location locationHero = hero.location;

        double speed = min(200, locationCurrent.measureSpeed()) * 0.625;
        double moveDistance = hero.shipInfo.speed * 0.625 + speed;
        double centerDistance = current.type.radius + (current.health.hpPercent() > 0.25 || !locationCurrent.isMoving() ? speed : -speed);
        double angle = locationCurrent.angle(locationHero);

        double x = locationCurrent.x - cos(angle) * centerDistance;
        double y = locationCurrent.y - sin(angle) * centerDistance;

        if ((moveDistance = (moveDistance - (locationHero.distance(x, y)))) > 0) {

            double add = moveDistance / centerDistance;

            angle += add;

            x = locationCurrent.x - cos(angle) * centerDistance;
            y = locationCurrent.y - sin(angle) * centerDistance;

        }

        hero.move(x, y);
    }

    private boolean hasEnemies() {
        for (Ship ship : ships) {
            if (ship.playerInfo.isEnemy()) {

                if (dangerous.contains(ship.id)) {
                    return true;
                } else if (ship.isAttacking(hero)) {
                    dangerous.add(ship.id);
                    return true;
                }

            }
        }
        return false;
    }

    private boolean isAttackedByOthers(Npc npc) {

        if (ignore.contains(npc.id)) return true;

        for (Ship ship : ships) {
            if (ship.isAttacking(npc)) {
                ignore.add(npc.id);
                return true;
            }
        }

        return false;
    }

    private Npc closestNpc(double x, double y) {
        double distance = 40000;
        Npc closest = null;

        for (Npc npc : npcs) {

            if (!npc.type.name.startsWith("Unknown") && !isAttackedByOthers(npc)) {
                double distanceCurrent = npc.location.distance(x, y);
                if (distanceCurrent < distance) {
                    distance = distanceCurrent;
                    closest = npc;
                }

            }
        }

        return closest;
    }
}
