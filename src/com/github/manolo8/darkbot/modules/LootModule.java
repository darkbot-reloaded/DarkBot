package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Module;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Map;

import java.util.HashSet;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

public class LootModule extends Module {

    private Npc current;
    private long laserTime;
    private boolean sab;

    private Set<Integer> dangerous;

    public LootModule(Main main) {
        super(main);

        dangerous = new HashSet<>();
    }

    @Override
    public void install() {
        main.guiManager.module = null;
        main.guiManager.nullPetModuleOnActivate = false;
    }

    @Override
    public void tick() {

        boolean mapWrong = main.starManager.starSystem.get(main.config.WORKING_MAP) != main.mapManager.map;
        boolean cont = current == null || (current.isInvalid() || current.health.hp == 0 || current.health.hp > 300000);
        //If is ally attack, just ignore, let he kill to lost honor!
        boolean flee = main.config.RUN_FROM_ENEMIES && ((main.hero.health.healthPercent() < 0.5 && cont) || hasEnemies());

        if (mapWrong || flee) {

            //Check if HP is not decreased in last 5 seconds
            //If is not, hero is in safe area, so, wait
            if (mapWrong || main.hero.health.isDecreasedIn(3000)) {
                main.setModule(new MapModule(main))
                        .setTarget(main.starManager.starSystem.get(main.config.WORKING_MAP),
                                () -> main.setModule(this)
                        );
            }

            return;
        }

        Npc closest = closest(main.hero.location);

        if (current == null || current.isInvalid() || (closest != null && current != null && current.health.hp == 0)) {

            if (closest == null) {

                if (!main.hero.isMoving()) {
                    main.hero.moveRandom();
                }

                current = null;

            } else {
                current = closest;
            }

        }

        if (current != null) {

            double distance = current.location.distance(main.hero);

            if (((distance < current.type.radius || distance > current.type.radius))) {
                move(
                        current.location,
                        600,
                        (current.health.healthPercent() < 0.25 | current.health.hp == 0) ? current.type.radius / 1.5 : current.type.radius
                );
            }

            if (main.hero.isTarget(current)) {

                main.hero.attackMode();

                long laser = System.currentTimeMillis() - laserTime;

                if ((!main.hero.isAttacking(current) && laser > 1000)
                        || (!current.health.isDecreasedIn(7000) && laser > 7000)) {

                    //Re-check if is start shot again
                    if (attackedByOthers(current)) {
                        current = null;
                        return;
                    }

                    API.button('Z');
                    laserTime = System.currentTimeMillis();
                }

                if (main.config.AUTO_SAB && main.hero.health.shieldPercent() < 0.4 && current.health.shield > 12000) {

                    if (!sab) {
                        API.button(main.config.AUTO_SAB_KEY);
                        sab = true;
                    }

                } else if (sab) {
                    API.button(main.config.AMMO_KEY);
                    sab = false;
                }

            } else if (main.hero.location.distance(current) < 800) {
                main.hero.setTarget(current);
            }

        }

    }

    private void move(Location location, double moveDistance, double centerDistance) {

        Location hero = main.hero.location;

        double angle = location.angle(hero);

        Location target = new Location(
                location.x - cos(angle) * centerDistance,
                location.y - sin(angle) * centerDistance
        );

        moveDistance = moveDistance - target.distance(hero);

        if (moveDistance > 0) {

            angle += moveDistance / centerDistance;

            target.x = location.x - cos(angle) * centerDistance;
            target.y = location.y - sin(angle) * centerDistance;
        }

        main.hero.move(target);
    }

    private boolean hasEnemies() {
        for (Ship ship : main.mapManager.ships) {
            if (ship.isEnemy()) {

                if (dangerous.contains(ship.id)) {
                    return true;
                } else if (ship.isAttacking(main.hero)) {
                    dangerous.add(ship.id);
                    return true;
                }

            }
        }
        return false;
    }

    private boolean attackedByOthers(Npc npc) {
        for (Ship ship : main.mapManager.ships) {
            if (ship.isAttacking(npc)) {
                return true;
            }
        }

        return false;
    }

    private Npc closest(Location location) {
        double distance = 40000;
        Npc closest = null;

        for (Npc npc : main.mapManager.npcs) {

            if (npc.type.name.startsWith("Unknown")) continue;

            if (attackedByOthers(npc)) continue;

            double distanceCurrent = location.distance(npc.location);
            if (distanceCurrent < distance) {
                distance = distanceCurrent;
                closest = npc;
            }
        }

        return closest;
    }
}
