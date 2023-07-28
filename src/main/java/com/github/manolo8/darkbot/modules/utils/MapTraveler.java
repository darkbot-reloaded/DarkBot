package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;

import java.util.List;
import java.util.function.Consumer;

public class MapTraveler {

    private final Main main;
    private final HeroManager hero;
    private final List<Portal> portals;
    private final StarManager star;
    private final Consumer<Map> listener = this::onMapChange;
    protected PortalJumper jumper;

    public Portal current;
    public Map target;
    private int lastPortals;
    private long shipTpWait = -1, mapChangeWait = -1;
    private boolean done;

    public MapTraveler(Main main) {
        this.hero = main.hero;
        this.portals = main.mapManager.entities.portals;
        this.star = main.starManager;
        this.main = main;
        this.jumper = new PortalJumper(hero);
        main.mapManager.mapChange.add(listener);
    }

    public void uninstall() {
        main.mapManager.mapChange.remove(listener);
    }

    public void setTarget(Map target) {
        shipTpWait = mapChangeWait = -1;
        this.target = target;
        this.done = false;
        this.jumper.reset();
    }

    public boolean isDone() {
        return done;
    }

    public void tick() {
        if (hero.map == target) {
            done = true;
            return;
        }

        if (hero.locationInfo.now.distance(hero.locationInfo.last) > 5000)
            shipTpWait = System.currentTimeMillis() + 2000;

        if ((shipTpWait == -1) != (mapChangeWait == -1)) {
            if (System.currentTimeMillis() < Math.max(shipTpWait, mapChangeWait)) return;
        }
        if (current == null || current.removed || lastPortals != portals.size()) {
            current = star.next(hero, target);
            lastPortals = portals.size();
            jumper.reset();
        }

        if (current == null) {
            if (System.currentTimeMillis() - mapChangeWait > 3000) done = true; // No port found after 3 secs, just go back.
            return;
        }
        shipTpWait = mapChangeWait = -1;

        if (current.locationInfo.distance(hero) > 1500) // Portal very close, no need to disable pet
            main.guiManager.pet.setEnabled(false);
        hero.runMode();

        jumper.travelAndJump(current);
    }

    private void onMapChange(Map map) {
        mapChangeWait = System.currentTimeMillis() + 2000;
        lastPortals = -1;
    }

}
