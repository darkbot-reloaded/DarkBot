package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

public class PortalJumper {

    private final HeroManager hero;
    private final Drive drive;
    private Portal last;
    private long nextMoveClick;

    public PortalJumper(HeroManager hero) {
        this.hero = hero;
        this.drive = hero.drive;
    }

    public void reset() {
        this.last = null;
    }

    public boolean travel(Portal target) {
        double leniency = Math.min(200 + drive.closestDistance(target.locationInfo.now), 600);
        if (target.locationInfo.isLoaded() && drive.movingTo().distance(target.locationInfo.now) > leniency) {
            drive.move(Location.of(target.locationInfo.now, Math.random() * Math.PI * 2, Math.random() * 200));
            return false;
        }
        return hero.locationInfo.distance(target) <= leniency && !drive.isMoving();
    }

    public void jump(Portal target) {
        hero.jumpPortal(target);

        if (target != last) {
            last = target;
            nextMoveClick = System.currentTimeMillis() + 5000;
        } else if (System.currentTimeMillis() > nextMoveClick && !target.clickable.enabled) {
            hero.drive.clickCenter(true, target.locationInfo.now);
            nextMoveClick = System.currentTimeMillis() + 10000;
        }
    }

    public void travelAndJump(Portal target) {
        if (travel(target)) jump(target);
    }

}
