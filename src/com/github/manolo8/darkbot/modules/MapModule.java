package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.itf.MapChange;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;

import static com.github.manolo8.darkbot.Main.API;

public class MapModule implements Module, MapChange {


    private Main main;
    private HeroManager hero;
    private StarManager star;

    private Module back;
    private Portal current;
    private Map target;
    private long time;

    @Override
    public void install(Main main) {
        this.hero = main.hero;
        this.star = main.starManager;
        this.main = main;
    }

    public void setTarget(Map target) {
        this.target = target;

        current = star.next(hero.map, hero.location, target);
    }

    public void setTargetAndBack(Map target, Module back) {
        setTarget(target);
        this.back = back;
    }

    @Override
    public void tick() {

        if (current != null) {

            double distance = current.location.distance(hero);

            hero.runMode();

            if (distance < 100 && !hero.isMoving()) {

                if (hero.nextMap() != current.target.id && System.currentTimeMillis() - time > 9000) {
                    API.keyboardClick('J');
                    time = System.currentTimeMillis();
                }

            } else if (current.location.isLoaded()) {
                hero.move(current);
            }
        }

    }

    @Override
    public void onMapChange() {
        if (hero.map == target) {
            if (back != null) {
                main.setModule(back);
                back = null;
            }
            current = null;
        } else {
            current = star.next(hero.map, hero.location, target);
        }
    }
}
