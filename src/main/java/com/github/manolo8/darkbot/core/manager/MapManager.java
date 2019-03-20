package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.MapChange;
import com.github.manolo8.darkbot.core.utils.ClickPoint;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.core.utils.Location;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Math.random;

public class MapManager implements Manager {

    private final MouseManager mouseManager;
    private final Main main;

    public final EntityList entities;

    private long mapAddressStatic;
    private long viewAddressStatic;
    public long mapAddress;
    private long viewAddress;
    private long boundsAddress;
    long eventAddress;

    public static int id;

    public ZoneInfo preferred;
    public ZoneInfo avoided;

    public static int internalWidth = 15000;
    public static int internalHeight = 10000;

    public static int clientWidth;
    public static int clientHeight;

    public double boundX;
    public double boundY;
    public double boundMaxX;
    public double boundMaxY;
    public double width;
    public double height;

    public MapManager(Main main) {
        this.main = main;
        this.mouseManager = new MouseManager(this);
        this.mouseManager.start();

        this.entities = new EntityList(main);
    }


    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> {
            mapAddressStatic = value + 256;
            viewAddressStatic = value + 216;
            eventAddress = value + 200;
        });

    }

    public void tick() {
        long temp = API.readMemoryLong(mapAddressStatic);

        if (mapAddress != temp) {
            update(temp);
        }

        entities.update();

        updateBounds();
        checkMirror();
    }

    private void update(long address) {

        mapAddress = address;

        internalWidth = API.readMemoryInt(address + 68);
        internalHeight = API.readMemoryInt(address + 72);
        int tempId = API.readMemoryInt(address + 76);
        entities.update(address);

        if (tempId != id) {
            id = tempId;
            main.hero.map = main.starManager.byId(id);
            preferred = ConfigEntity.INSTANCE.getOrCreatePreferred();
            avoided = ConfigEntity.INSTANCE.getOrCreateAvoided();

            if (main.module instanceof MapChange) {
                ((MapChange) main.module).onMapChange();
            }
        }
    }

    void checkMirror() {
        long temp = API.readMemoryLong(eventAddress) + 4 * 14;

        if (API.readMemoryBoolean(temp)) {
            API.writeMemoryInt(temp, 0);
        }
    }

    void updateBounds() {

        long temp = API.readMemoryLong(viewAddressStatic);

        if (viewAddress != temp) {
            viewAddress = temp;
            boundsAddress = API.readMemoryLong(viewAddress + 208);
        }

        clientWidth = API.readMemoryInt(boundsAddress + 168);
        clientHeight = API.readMemoryInt(boundsAddress + 172);

        long updated = API.readMemoryLong(boundsAddress + 280);
        updated = API.readMemoryLong(updated + 112);

        boundX = API.readMemoryDouble(updated + 80);
        boundY = API.readMemoryDouble(updated + 88);
        boundMaxX = API.readMemoryDouble(updated + 112);
        boundMaxY = API.readMemoryDouble(updated + 120);
        width = boundMaxX - boundX;
        height = boundMaxY - boundY;
    }

    public boolean isTarget(Entity entity) {
        return API.readMemoryLong(API.readMemoryLong(mapAddress + 120) + 40) == entity.address;
    }

    public boolean isOutOfMap(double x, double y) {
        return x < 0 || y < 0 || x > internalWidth || y > internalHeight;
    }

    public boolean isCurrentTargetOwned() {
        long temp = API.readMemoryLong(viewAddressStatic);
        temp = API.readMemoryLong(temp + 216);
        temp = API.readMemoryLong(temp + 200);
        temp = API.readMemoryLong(temp + 48);
        return API.readMemoryInt(temp + 40) == 1;
    }

    public void mouseClick(Location loc) {
        this.mouseManager.click(loc);
    }

    public void clickCenter(boolean single) {
        ClickPoint clickPoint = clickPoint();
        //System.out.println("Simple click: " + clickPoint.x + "," + clickPoint.y + (single ? "" : " double"));
        API.mouseClick(clickPoint.x, clickPoint.y);
        if (!single) API.mouseClick(clickPoint.x, clickPoint.y);
    }

    ClickPoint clickPoint() {
        double x = (double) MapManager.clientWidth / 2 + (Math.random() - 0.5 * 40) * clientWidth / width,
            y = (double) MapManager.clientHeight / 2 + (Math.random() - 0.5 * 40) * clientHeight / height;
        return new ClickPoint((int) x, (int) y);
    }

}
