package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Module;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.MapManager;
import com.github.manolo8.darkbot.core.objects.Location;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.Math.sin;


public class GateModule extends Module {

    private Npc current;
    private long laserTime;
    private boolean repairing;
    private long portalWait;

    private Location zero;

    public GateModule(Main main) {
        super(main);

        this.zero = new Location(11000, 6000);
    }

    @Override
    public void install() {
        main.guiManager.module = null;
        main.guiManager.nullPetModuleOnActivate = false;
    }

    @Override
    public void tick() {


        if ((MapManager.id == 51 || MapManager.id == 52 || MapManager.id == 53) && hasNPC()) {

            Npc closest = closest(main.hero.location);

            double distance = closest.location.distance(main.hero);

            if (repairing || main.hero.health.healthPercent() < 0.6) {

                main.hero.runMode();

                move(zero, 5000, 1000);

                repairing = main.hero.health.healthPercent() < 0.9;

                return;

            } else if (closest.location.x == 0 && closest.location.y == 0) {
                main.hero.move(395, 395);
            } else if (closest.location.x == 21000 && closest.location.y == 13500) {
                main.hero.move(20605, 13105);
            } else if (distance < closest.type.radius) {
                move(zero, 5000, closest.type.radius);
            } else {
                move(closest.location, closest.type.radius, closest.type.radius - distance);
            }

            if (current == null || current.isInvalid()) {
                current = closest;
            } else {
                double distanceCurrent = current.location.distance(main.hero);

                if (distanceCurrent - 120 > distance || current.health.healthPercent() < 0.25) {
                    current = closest;
                }
            }

            if (main.hero.isTarget(current)) {

                main.hero.attackMode();

                long laser = System.currentTimeMillis() - laserTime;

                if ((!main.hero.isAttacking(current) && laser > 1000)
                        || (!current.health.isDecreasedIn(4000) && laser > 4000)) {

                    API.button('Z');
                    laserTime = System.currentTimeMillis();

                }

            } else if (main.hero.location.distance(current) < 800) {
                main.hero.setTarget(current);
            }

        } else {

            if (!main.hero.isMoving()) {

                Portal portal = main.mapManager.closestByType(2, 3, 4);

                if (portal != null && portal.location.isLoaded()) {
                    main.hero.move(portal.location);

                    if (portal.location.distance(main.hero) < 200 && System.currentTimeMillis() - portalWait > 3000) {
                        API.button('J');
                        portalWait = System.currentTimeMillis();
                    }

                }

            }

        }

    }

    private void tickAttackMode() {

    }

    private void tickKamikazeMode() {

    }

    private void move(Location base, double radius, double distance) {
        Location hero = main.hero.location;

        double angle = base.angle(hero) + distance / radius;

        Location target = new Location(
                base.x - cos(angle) * radius,
                base.y - sin(angle) * radius
        );

        main.hero.move(target);
    }

    private boolean hasNPC() {
        return main.mapManager.npcs.size() != 0;
    }

    private Npc closest(Location location) {
        double distance = 40000;
        Npc closest = null;

        for (Npc npc : main.mapManager.npcs) {
            double distanceCurrent = location.distance(npc.location);
            if (distanceCurrent < distance) {
                distance = distanceCurrent;
                closest = npc;
            }
        }

        return closest;
    }
}
