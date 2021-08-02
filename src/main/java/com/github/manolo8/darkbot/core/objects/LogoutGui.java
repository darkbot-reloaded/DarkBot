package com.github.manolo8.darkbot.core.objects;

public class LogoutGui extends Gui {

    private long lastShown;

    public long getLastShown() {
        return lastShown;
    }

    @Override
    public boolean show(boolean value) {
        if (value) lastShown = System.currentTimeMillis();
        return super.show(value);
    }
}
