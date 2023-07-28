package com.github.manolo8.darkbot.modules.utils;

import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.manager.HeroManager;

// Deprecated in favor of API implementation of PortalJumper.
// Kept for backwards compatibility, but now simply redirects to new impl.
@Deprecated
public class PortalJumper {

    private final eu.darkbot.shared.utils.PortalJumper jumper;

    public PortalJumper(HeroManager hero) {
        this.jumper = new eu.darkbot.shared.utils.PortalJumper(hero.main.pluginAPI);
    }

    public void reset() {
        jumper.reset();
    }

    public boolean travel(Portal target) {
        return jumper.travel(target);
    }

    public void jump(Portal target) {
        jumper.jump(target);
    }

    public void travelAndJump(Portal target) {
        jumper.travelAndJump(target);
    }

}
