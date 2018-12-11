package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.core.manager.MapManager;

import static com.github.manolo8.darkbot.Main.API;

public class Gui {

    public final long address;

    public final String name;
    public boolean visible;

    private Point minimized;
    private Location size;
    private Location pos;

    public int x;
    public int y;
    public int width;
    public int height;

    private long time;

    public Gui(long address, String name) {

        this.address = address;
        this.name = name;

        this.size = new Location(0);
        this.pos = new Location(0);
        this.minimized = new Point(0);

        update();
    }

    public void update() {


        size.update(API.readMemoryLong(address + 10 * 8));
        pos.update(API.readMemoryLong(address + 9 * 8));
        minimized.update(API.readMemoryLong(address + 14 * 8));

        size.update();
        pos.update();
        minimized.update();

        width = (int) Math.round(size.x);
        height = (int) Math.round(size.y);
        x = (int) Math.round((MapManager.clientWidth - size.x) * 0.01 * pos.x);
        y = (int) Math.round((MapManager.clientHeight - size.y) * 0.01 * pos.y);

        visible = API.readMemoryBoolean(address + 32);
    }

    public boolean show(boolean value) {

        if (value != visible) {

            if (System.currentTimeMillis() - 1000 > time) {
                API.mouseClick(minimized.x + 5, minimized.y = 5);
                time = System.currentTimeMillis();
            }

            return false;
        }

        return System.currentTimeMillis() - 1000 > time;
    }

    public boolean isInside(double x, double y) {

        if (visible) {

            int gmx = this.x + width;
            int gmy = this.y + height;

            return x >= this.x && x <= gmx && y >= this.y && y <= gmy;
        } else {
            return x >= minimized.x && y >= minimized.y && x <= minimized.x + 38 && y <= minimized.y + 38;
        }

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
            this.pixels = API.pixels(x, y, width, height);
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