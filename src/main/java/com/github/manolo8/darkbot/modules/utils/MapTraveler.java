package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.List;
import java.util.function.Consumer;

public class MapTraveler {

    private Main main;
    private HeroManager hero;
    private List<Portal> portals;
    private Drive drive;
    private StarManager star;
    private Consumer<Map> listener = this::onMapChange;

    public Portal current;
    public Map target;
    private int lastPortals;
    private long lastJumpStart;
    private long shipTpWait = -1, mapChangeWait = -1;
    private boolean done;

    public MapTraveler(Main main) {
        this.hero = main.hero;
        this.portals = main.mapManager.entities.portals;
        this.drive = main.hero.drive;
        this.star = main.starManager;
        this.main = main;
        main.mapManager.mapChange.add(listener);
    }

    public void uninstall() {
        main.mapManager.mapChange.remove(listener);
    }

    public void setTarget(Map target) {
        shipTpWait = mapChangeWait = -1;
        this.target = target;
        this.done = false;
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
            lastJumpStart = 0;
        }

        if (current == null) {
            if (System.currentTimeMillis() - mapChangeWait > 3000) done = true; // No port found after 3 secs, just go back.
            return;
        }
        shipTpWait = mapChangeWait = -1;

        if (current.locationInfo.distance(hero) > 1500) // Portal very close, no need to disable pet
            main.guiManager.pet.setEnabled(false);
        hero.runMode();

        if (!moveToCurrent()) return;
        hero.jumpPortal(current);

        if (lastJumpStart == 0) {
            lastJumpStart = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastJumpStart > 5_000) {
            hero.drive.clickCenter(true, current.locationInfo.now);
            lastJumpStart = System.currentTimeMillis();
        }
    }

    private boolean moveToCurrent() {
        double leniency = Math.min(200 + drive.closestDistance(current.locationInfo.now), 600);
        if (current.locationInfo.isLoaded() && drive.movingTo().distance(current.locationInfo.now) > leniency) {
            drive.move(Location.of(current.locationInfo.now, Math.random() * Math.PI * 2, Math.random() * 200));
            return false;
        }
        return hero.locationInfo.distance(current) <= leniency && !drive.isMoving();
    }

    private void onMapChange(Map map) {
        mapChangeWait = System.currentTimeMillis() + 2000;
        lastPortals = -1;
    }

}
