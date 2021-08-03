package com.github.manolo8.darkbot.core.objects;

public class LogoutGui extends Gui {

    private long lastShown;

    public long getLastShown() {
        return lastShown;
    }

    @Override
    public void update() {
        super.update();
        // Due to bugs in logout gui, make it so isAnimationDone will only be
        // true a while after tweening, instead of instantly, by updating time
        if (isTweening) time = System.currentTimeMillis();
    }

    @Override
    public boolean show(boolean value) {
        if (value) lastShown = System.currentTimeMillis();
        return super.show(value);
    }
}
