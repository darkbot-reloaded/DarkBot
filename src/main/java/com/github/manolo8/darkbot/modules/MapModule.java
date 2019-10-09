package com.github.manolo8.darkbot.modules;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.itf.MapChange;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import java.util.List;

public class MapModule extends TemporalModule implements MapChange {

    private Main main;
    private HeroManager hero;
    private List<Portal> portals;
    private Drive drive;
    private StarManager star;

    private Portal current;
    private Map target;
    private int lastPortals;
    private long lastJumpStart;
    private long lastMapChange;

    @Override
    public void install(Main main) {
        super.install(main);
        this.hero = main.hero;
        this.portals = main.mapManager.entities.portals;
        this.drive = main.hero.drive;
        this.star = main.starManager;
        this.main = main;
    }

    @Override
    public String status() {
        return "Traveling to " + target.name + (current != null ? ", next map: " + current.target.name : "");
    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    public void setTarget(Map target) {
        lastMapChange = System.currentTimeMillis();
        this.target = target;
    }

    @Override
    public void tick() {
        if (hero.map == target) return;
        if (current == null || current.removed || lastPortals != portals.size()) {
            current = star.next(hero, target);
            lastPortals = portals.size();
            lastJumpStart = 0;
        }

        if (current == null) {
            if (System.currentTimeMillis() - lastMapChange > 3000) goBack();
            return;
        }

        if (current.locationInfo.distance(hero) > 1500) // Portal very close, no need to disable pet
            main.guiManager.pet.setEnabled(false);
        hero.runMode();

        if (!moveToCurrent(false)) return;
        hero.jumpPortal(current);

        if (lastJumpStart == 0) {
            lastJumpStart = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastJumpStart > 5000) {
            moveToCurrent(true);
            lastJumpStart = System.currentTimeMillis();
        }
    }

    private boolean moveToCurrent(boolean forceMove) {
        double leniency = Math.min(200 + drive.closestDistance(current.locationInfo.now), 600);
        if (current.locationInfo.isLoaded() && (forceMove || drive.movingTo().distance(current.locationInfo.now) > leniency)) {
            drive.move(Location.of(current.locationInfo.now, Math.random() * Math.PI * 2, Math.random() * 200));
            return false;
        }
        return hero.locationInfo.distance(current) <= leniency && !drive.isMoving();
    }

    @Override
    public void onMapChange() {
        lastMapChange = System.currentTimeMillis();
        lastPortals = -1;
        if (hero.map == target) goBack();
    }

    @Override
    protected void goBack() {
        super.goBack();
        current = null;
        lastPortals = -1;
    }
}
