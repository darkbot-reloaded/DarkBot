package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.MapChange;
import com.github.manolo8.darkbot.core.utils.EntityList;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager {

    private final Main main;

    public final EntityList entities;

    private long mapAddressStatic;
    private long viewAddressStatic;
    public long mapAddress;
    private long viewAddress;
    private long boundsAddress;
    private long eventAddress;

    public static int id;

    public static int internalWidth;
    public static int internalHeight;

    public static int clientWidth;
    public static int clientHeight;

    public double boundX;
    public double boundY;
    public double boundMaxX;
    public double boundMaxY;

    public MapManager(Main main) {
        this.main = main;

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
            main.hero.map = main.starManager.fromId(id);

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

    public void translateMouseMove(double x, double y) {
        API.mouseMove(
                (int) ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                (int) ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }

    public void translateMousePress(double x, double y) {
        API.mousePress(
                (int) ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                (int) ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }

    public void translateMouseClick(double x, double y) {
        API.mouseClick(
                (int) ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                (int) ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }

    public void translateMouseMoveRelease(double x, double y) {
        API.mouseRelease(
                (int) ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                (int) ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }
}
