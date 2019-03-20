package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.utils.ClickPoint;
import com.github.manolo8.darkbot.core.utils.Location;

public class MouseManager extends Thread {

    private final Object LOCK = new Object();
    private ClickPoint clickPoint = new ClickPoint(0, 0);
    private volatile long address;
    private volatile long runUntil;

    private final MapManager map;

    public MouseManager(MapManager map) {
        super("MouseClicker");
        this.map = map;
    }

    public void click(Location loc) {
        clickPoint.x = (int) loc.x;
        clickPoint.y = (int) loc.y;

        //System.out.println("Complex click: " + clickPoint.x + "," + clickPoint.y);

        ClickPoint click = map.clickPoint();

        address = Main.API.readMemoryLong(Main.API.readMemoryLong(map.eventAddress) + 64L);

        runUntil = System.currentTimeMillis() + 200;
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
        Main.API.mouseClick(click.x, click.y);
        runUntil = System.currentTimeMillis() + 20;
    }

    public final void run() {
        while (true) {
            synchronized (this.LOCK) {
                if (this.runUntil < System.currentTimeMillis()) {
                    try {
                        this.LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Main.API.writeMemoryDouble(address + 32L, (double) clickPoint.x);
                Main.API.writeMemoryDouble(address + 40L, (double) clickPoint.y);
            }
        }
    }

}
