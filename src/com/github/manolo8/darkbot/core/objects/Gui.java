package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.MapManager;

import static com.github.manolo8.darkbot.Main.API;

public class Gui implements Updatable {

    public long address;
    public long addressInfo;


    public boolean visible;

    private Point minimized;
    private Location size;
    private Location pos;

    public int x;
    public int y;
    public int width;
    public int height;

    private long time;

    public Gui(long address) {

        this.address = address;

        this.size = new Location(0);
        this.pos = new Location(0);
        this.minimized = new Point(0);

        update();
    }

    public void update() {

        if (address != 0) {
            size.update(API.readMemoryLong(addressInfo + 10 * 8));
            pos.update(API.readMemoryLong(addressInfo + 9 * 8));
            minimized.update(API.readMemoryLong(addressInfo + 14 * 8));

            size.update();
            pos.update();
            minimized.update();

            width = (int) Math.round(size.x);
            height = (int) Math.round(size.y);
            x = (int) Math.round((MapManager.clientWidth - size.x) * 0.01 * pos.x);
            y = (int) Math.round((MapManager.clientHeight - size.y) * 0.01 * pos.y);

            visible = API.readMemoryBoolean(addressInfo + 32);
        }
    }

    @Override
    public void update(long address) {

        if (address == 0) {
            reset();
        } else {
            this.address = address;
            this.addressInfo = API.readMemoryLong(address + 488);
        }
    }

    public void reset() {
        this.address = 0;
        this.visible = false;
        this.height = 0;
        this.width = 0;
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

    public PixelHelper createHelper(int plusWidth, int plusHeight) {
        return new PixelHelper(width + plusWidth, height + plusHeight);
    }

    public class PixelHelper {

        public int[] pixels;
        public int size;

        public int width;
        public int height;

        PixelHelper(int width, int height) {
//            this.pixels = API.pixels(x, y, width, height);
            this.width = width;
            this.height = height;
            this.size = width * height;
        }

        public int add(int pixel, int x, int y) {

            pixel = pixel + x + y * width;

            return pixel < pixels.length && pixel > 0 ? pixels[pixel] : 0;
        }

        public int at(int x, int y) {
            return pixels[x + y * width];
        }

        public void click(int pixel) {
            API.mouseClick(x + (pixel % width), y + (pixel / width));
        }
    }
}