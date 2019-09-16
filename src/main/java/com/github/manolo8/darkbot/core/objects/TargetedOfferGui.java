package com.github.manolo8.darkbot.core.objects;

import static com.github.manolo8.darkbot.Main.API;

public class TargetedOfferGui extends Gui {

    private boolean initialized;
    private int offset = 0;

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
        offset = 0;
    }

    public boolean show(boolean value) {
        if (offset > 2) return visible == value;
        if (value) throw new UnsupportedOperationException("Can't set showing a targeted offer!");
        if (trySetShowing(false)) {
            if (offset == 0) API.mouseClick(255, 77);
            else if (offset == 1) API.mouseClick(280, 60);
            offset++;
            return false;
        }
        return isAnimationDone();
    }

    @Override
    public boolean isAnimationDone() {
        return System.currentTimeMillis() - 2000 > time;
    }
}