package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Module;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.objects.Location;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.pet.PetLoot;

import java.util.HashSet;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

public class CollectorModule extends Module {

    private Set<Integer> dangerous;

    private Box current;

    private long invisibleTime;
    private boolean clicked;

    public CollectorModule(Main main) {
        super(main);

        dangerous = new HashSet<>();
    }

    @Override
    public void install() {
        main.guiManager.module = new PetLoot();
        main.guiManager.nullPetModuleOnActivate = false;
    }

    @Override
    public void tick() {

        if (main.starManager.starSystem.get(main.config.WORKING_MAP) != main.mapManager.map) {

            main.hero.runMode();

            main.setModule(new MapModule(main))
                    .setTarget(main.starManager.starSystem.get(main.config.WORKING_MAP),
                            () -> main.setModule(this)
                    );

            return;
        }

        if (main.config.AUTO_CLOACK && !main.hero.invisible && System.currentTimeMillis() - invisibleTime > 60000) {
            invisibleTime = System.currentTimeMillis();
            API.button(main.config.AUTO_CLOACK_KEY);
        }

        if(main.config.STAY_AWAY_FROM_ENEMIES) {
            Ship dangerous = closestDangerous();

            if (dangerous != null) {
                away(dangerous.location);
                return;
            }
        }

        Box box = closest(main.hero.location);

        if (box != null) {

            if (box != current && (current == null || current.isInvalid() || current.ignore() || isBetter(box))) {
                current = box;
                current.trying();
                main.hero.move(box.location);
                clicked = false;
            } else {
                double distance = box.location.distance(main.hero.location);
                if (distance == 0) {
                    current.collected();
                } else if ((!main.hero.isMoving() || !clicked) && distance < 100) {
                    clicked = true;
                    main.hero.stop(false);
                    main.hero.click(box.location.add(0, 100));
                } else if (clicked && !main.hero.isMoving()) {
                    main.hero.move(box.location);
                    current.trying();
                    clicked = false;
                }
            }
        } else if (!main.hero.isMoving()) {
            current = null;
            main.hero.moveRandom();
        }

    }

    private void away(Location location) {
        Location hero = main.hero.location;

        double angle = location.angle(hero);
        double moveDistance = 600;

        Location target = new Location(
                location.x - cos(angle) * 3000,
                location.y - sin(angle) * 3000
        );

        moveDistance = moveDistance - target.distance(hero);

        if (moveDistance > 0) {

            angle += moveDistance / 3000;

            target.x = location.x - cos(angle) * 3000;
            target.y = location.y - sin(angle) * 3000;
        }

        main.hero.move(target);
    }

    private Box closest(Location location) {
        double distance = 40000;
        Box closest = null;

        for (Box box : main.mapManager.bonusBoxes) {
            if (box.ignore()) continue;
            double distanceCurrent = location.distance(box.location);
            if (distanceCurrent < distance) {
                distance = distanceCurrent;
                closest = box;
            }
        }

        return closest;
    }

    private Ship closestDangerous() {
        for (Ship ship : main.mapManager.ships) {
            if (ship.isEnemy() && !ship.invisible) {

                if (dangerous.contains(ship.id)) {
                    return ship;
                } else if (ship.isAttacking(main.hero)) {
                    dangerous.add(ship.id);
                    return ship;
                }

            }
        }
        return null;
    }

    private boolean isBetter(Box box) {
        double d1 = current.location.distance(main.hero); //1000
        double d2 = box.location.distance(main.hero); //700

        return d1 - 200 > d2;
    }

}
