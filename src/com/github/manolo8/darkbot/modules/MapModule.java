package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Do;
import com.github.manolo8.darkbot.core.def.MapChange;
import com.github.manolo8.darkbot.core.def.Module;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.objects.Map;

import static com.github.manolo8.darkbot.Main.API;

public class MapModule extends Module implements MapChange {

    private Map target;
    private Do run;

    private Portal current;
    private long time;
    private double lastDistance;

    public MapModule(Main main) {
        super(main);
    }

    @Override
    public void install() {
        main.guiManager.module = null;
        main.guiManager.nullPetModuleOnActivate = false;
    }

    public void setTarget(Map target) {
        this.target = target;

        current = main.starManager.next(main.mapManager.map, main.hero.location, target);

    }

    public void setTarget(Map target, Do run) {
        setTarget(target);
        this.run = run;
    }

    @Override
    public void tick() {

        if (current != null) {

            double distance = current.location.distance(main.hero);

            main.hero.runMode();

            if (distance < 250) {

                if (main.hero.nextMap() != current.target.id && System.currentTimeMillis() - time > 5000) {
                    API.button('J');
                    time = System.currentTimeMillis();
                }

            } else if ((!main.hero.isMoving() || lastDistance < distance) && current.location.isLoaded()) {
                main.hero.move(current);
            }

            lastDistance = distance;
        }

    }

    @Override
    public void onMapChange() {
        if (main.mapManager.map == target) {
            if (run != null) {
                run.run();
                run = null;
            }
            current = null;
        } else {
            current = main.starManager.next(main.mapManager.map, main.hero.location, target);
        }
    }
}
