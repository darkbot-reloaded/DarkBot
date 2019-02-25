package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.MapManager;

import static com.github.manolo8.darkbot.Main.API;

public class Gui extends Updatable {

    public long addressInfo;

    public boolean visible;

    private Point minimized;
    private LocationInfo size;
    private LocationInfo pos;

    public int x;
    public int y;
    public int width;
    public int height;

    private long time;
    private long update;

    public Gui() {
        this.size = new LocationInfo(0);
        this.pos = new LocationInfo(0);
        this.minimized = new Point(0);

        update();
    }

    public void update() {
        if (address == 0) return;
        size.update(API.readMemoryLong(addressInfo + 10 * 8));
        pos.update(API.readMemoryLong(addressInfo + 9 * 8));
        minimized.update(API.readMemoryLong(addressInfo + 14 * 8));

        size.update();
        pos.update();
        minimized.update();

        width = (int) Math.round(size.now.x);
        height = (int) Math.round(size.now.y);
        x = (int) Math.round((MapManager.clientWidth - size.now.x) * 0.01 * pos.now.x);
        y = (int) Math.round((MapManager.clientHeight - size.now.y) * 0.01 * pos.now.y);

        visible = API.readMemoryBoolean(addressInfo + 32);
    }

    @Override
    public void update(long address) {
        if (address == 0) {
            reset();
        } else {
            super.update(address);
            this.addressInfo = API.readMemoryLong(address + 488);
            this.update = System.currentTimeMillis();
        }
    }

    public void reset() {
        this.address = 0;
        this.visible = false;
        this.height = 0;
        this.width = 0;
    }

    public boolean lastUpdatedIn(long time) {
        return update != 0 && System.currentTimeMillis() - update > time;
    }

    public void click(int plusX, int plusY) {
        API.mouseClick(x + plusX, y + plusY);
    }

    public boolean show(boolean value) {

        if (value != visible) {

            if (System.currentTimeMillis() - 1000 > time) {
                API.mouseClick((int) minimized.x + 5, (int) minimized.y + 5);
                time = System.currentTimeMillis();
            }

            return false;
        }

        return System.currentTimeMillis() - 1000 > time;
    }
}