package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;

public class TargetedOfferGui extends Gui {

    private boolean initialized;

    public void update() {
        super.update();
        if (address == 0) return;

        // Reading position from Sprite should be more accurate
        //x = (int) API.readMemoryDouble(address + 0x250);
        //y = (int) API.readMemoryDouble(address + 0x258);

        if (!initialized) { // Initialized sets initial time to wait for animation when first displayed
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
            click(width - 3, 3);
            return false;
        }
        return isAnimationDone();
    }

    @Override
    public boolean isAnimationDone() {
        return System.currentTimeMillis() - 2000 > time;
    }
}