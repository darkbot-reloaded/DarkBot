package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Point;
import com.github.manolo8.darkbot.core.utils.ClickPoint;
import com.github.manolo8.darkbot.core.utils.Location;

public class MouseManager extends Thread {

    private final Object LOCK = new Object();
    private volatile boolean waiting = true;
    private ClickPoint clickPoint = new ClickPoint(0, 0);
    private Point mouse = new Point();

    private final MapManager map;

    public MouseManager(MapManager map) {
        super("MouseClicker");
        this.map = map;
    }

    public void click(Location now, Location loc) {
        mouse.update(Main.API.readMemoryLong(Main.API.readMemoryLong(map.eventAddress) + 64L));
        clickPoint.x = (int) loc.x;
        clickPoint.y = (int) loc.y;

        loc.toAngle(now, now.angle(loc), 100);
        ClickPoint clickPoint = map.clickPoint(loc);

        this.waiting = false;

        synchronized (LOCK) {
            LOCK.notifyAll();
        }

        Main.API.mouseClick(clickPoint.x, clickPoint.y);
        this.waiting = true;
    }

    public final void run() {
        while (true) {
            synchronized (this.LOCK) {
                if (this.waiting) {
                    try {
                        this.LOCK.wait();
                    } catch (InterruptedException interruptedexception) {
                        interruptedexception.printStackTrace();
                    }
                }

                Main.API.writeMemoryDouble(mouse.address + 32L, (double) clickPoint.x);
                Main.API.writeMemoryDouble(mouse.address + 40L, (double) clickPoint.y);
            }
        }
    }

}
