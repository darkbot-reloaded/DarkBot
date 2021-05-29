package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.GroupManager;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.Drive;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;

public class PortalJumper {

    private final HeroManager hero;
    private final Drive drive;
    private Portal last;
    private long nextMoveClick;
    private long tryingToJumpSince;

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
        // Low & hades, wait for group before trying to jump
        // This prevents the J key being written while typing out player names for invites
        int minGroupSize = target.target == null ? 0
                : target.target.id == 200 ? 3 // LoW
                : target.target.id == 203 ? 4 // Hades
                : 0;

        if (minGroupSize > 0) {
            GroupManager gm = hero.main.guiManager.group;
            if (!gm.group.isValid() || gm.group.size < minGroupSize) return;
        }

        if (target != last) {
            last = target;
            tryingToJumpSince = System.currentTimeMillis();
            nextMoveClick = System.currentTimeMillis() + 5000;
        } else if (System.currentTimeMillis() > nextMoveClick && !target.clickable.enabled) {
            hero.drive.clickCenter(true, target.locationInfo.now);
            nextMoveClick = System.currentTimeMillis() + 10000;
        }

        if (tryingToJumpSince != 0 && System.currentTimeMillis() > tryingToJumpSince + 120000) {
            System.out.println("Triggering refresh: jumping portal took too long");
            tryingToJumpSince = 0;
            API.handleRefresh();
        }

        hero.jumpPortal(target);
    }

    public void travelAndJump(Portal target) {
        if (travel(target)) jump(target);
    }

}
