package com.github.manolo8.darkbot.core.objects;

import static com.github.manolo8.darkbot.Main.API;

public class TargetedOfferGui extends Gui {

    private boolean initialized;

    public void update() {
        super.update();
        if (address == 0) return;
        if (!initialized) {
            time = System.currentTimeMillis();
            initialized = true;
        }
        visible = true;
    }

    @Override
    public void reset() {
        super.reset();
        initialized = false;
    }

    public boolean show(boolean value) {
        if (value) throw new UnsupportedOperationException("Can't set showing a targeted offer!");
        if (trySetShowing(false)) {
            API.mouseClick(this.x + this.width, 77);
            return false;
        }
        return isAnimationDone();
    }

    @Override
    public boolean isAnimationDone() {
        return System.currentTimeMillis() - 2000 > time;
    }
}