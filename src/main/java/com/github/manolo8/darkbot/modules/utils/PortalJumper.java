package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;

public class PortalJumper {

    private final HeroManager hero;
    private Portal last;
    private long nextMoveClick;

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
            nextMoveClick = System.currentTimeMillis() + 5000;
        } else if (System.currentTimeMillis() > nextMoveClick && !target.clickable.enabled) {
            hero.drive.clickCenter(true, target.locationInfo.now);
            nextMoveClick = System.currentTimeMillis() + 10000;
        }
    }

}
