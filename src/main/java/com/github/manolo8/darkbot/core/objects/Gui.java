package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.MapManager;

import static com.github.manolo8.darkbot.Main.API;

public class Gui extends Updatable {

    public long addressInfo;

    public boolean visible;
    protected boolean isTweening; // If it's in the middle of an animation

    protected Point pos;
    protected Point size;
    protected Point minimized;

    public int x;
    public int y;
    public int width;
    public int height;

    protected long time;
    protected long update;

    public Gui() {
        this.size = new Point();
        this.pos = new Point();
        this.minimized = new Point();

        update();
    }

    public void update() {
        if (address == 0) return;
        pos.update(API.readMemoryLong(addressInfo + 9 * 8));
        size.update(API.readMemoryLong(addressInfo + 10 * 8));
        // 11 * 8 = FeatureDefinitionVo
        // 12 * 8 = help text
        // 13 * 8 = tool tip
        minimized.update(API.readMemoryLong(addressInfo + 14 * 8));

        size.update();
        pos.update();
        minimized.update();

        width = (int) Math.round(size.x);
        height = (int) Math.round(size.y);
        // Set pos relative to window size
        x = (int) Math.round((MapManager.clientWidth - size.x) * 0.01 * pos.x);
        y = (int) Math.round((MapManager.clientHeight - size.y) * 0.01 * pos.y);

        visible = API.readMemoryBoolean(addressInfo + 32); // Maximized
        // API.readMemoryBoolean(addressInfo + 36); // Toggle maximize (set to true/false when pressing H to show/hide)
        // API.readMemoryBoolean(addressInfo + 40); // Maximized changed (set to true when toggling maximized)
        // API.readMemoryBoolean(addressInfo + 44); // Settings on server
        // API.readMemoryBoolean(addressInfo + 48); // show on top

        isTweening = API.readMemoryBoolean(address + 0xC4);
    }

    @Override
    public void update(long address) {
        if (address == 0) {
            reset();
        } else {
            super.update(address);
            this.addressInfo = API.readMemoryLong(address + 496);
            this.update = System.currentTimeMillis();
        }
    }

    public void reset() {
        this.address = 0;
        this.visible = false;
        this.height = 0;
        this.width = 0;
        this.update = 0;
    }

    public boolean lastUpdatedIn(long time) {
        return update != 0 && System.currentTimeMillis() - update > time;
    }

    public void click(int plusX, int plusY) {
        API.mouseClick(x + plusX, y + plusY);
    }

    public void hover(int plusX, int plusY) {
        API.mouseMove(x + plusX, y + plusY);
    }

    public boolean show(boolean value) {
        if (trySetShowing(value)) {
            if (minimized.address != 0) API.mouseClick((int) minimized.x + 5, (int) minimized.y + 5);
            return false;
        }
        return isAnimationDone();
    }

    /**
     * @param value Desired visibility status
     * @return If action should be taken to change the visibility status
     */
    public boolean trySetShowing(boolean value) {
        if (value != visible && isAnimationDone()) {
            time = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean isAnimationDone() {
        return !isTweening && System.currentTimeMillis() - 1000 > time;
    }

}