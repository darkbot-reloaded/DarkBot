package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;

public class PortalJumper {

    private HeroManager hero;
    private Portal last;
    private long lastJumpStart;

    public PortalJumper(HeroManager hero) {
        this.hero = hero;
    }

    public void reset() {
        this.last = null;
    }

    public void jump(Portal target) {
        hero.jumpPortal(target);

        if (target != last) {
            last = target;
            lastJumpStart = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - lastJumpStart > 5_000) {
            hero.drive.clickCenter(true, target.locationInfo.now);
            lastJumpStart = System.currentTimeMillis();
        }
    }

}
