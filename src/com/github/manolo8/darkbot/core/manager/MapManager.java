package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.MapChange;
import com.github.manolo8.darkbot.core.objects.swf.Array;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager {

    private long mapAddressStatic;
    private long viewAddressStatic;
    public static long mapAddress;
    private long viewAddress;
    private long boundsAddress;

    private Main main;
    private Array entities;

    private HashSet<Integer> ids;

    public List<Box> boxes;

    public List<Npc> npcs;
    public List<Portal> portals;
    public List<Ship> ships;
    public List<BattleStation> battleStations;

    public List<Entity> unknown;

    public static int id;

    public int internalWidth;
    public int internalHeight;

    public static int clientWidth;
    public static int clientHeight;

    public double boundX;
    public double boundY;
    public double boundMaxX;
    public double boundMaxY;

    public MapManager(Main main) {
        this.main = main;

        this.entities = new Array(0);

        this.ids = new HashSet<>();

        this.boxes = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.portals = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.unknown = new ArrayList<>();
        this.battleStations = new ArrayList<>();
    }


    @Override
    public void install(BotInstaller botInstaller) {

        botInstaller.screenManagerAddress.add(value -> {
            mapAddressStatic = value + 256;
            viewAddressStatic = mapAddressStatic - 40;
        });

    }

    @Override
    public void stop() {
        cleanup();
    }

    public void tick() {
        long temp = API.readMemoryLong(mapAddressStatic);

        synchronized (Main.UPDATE_LOCKER) {

            if (mapAddress != temp) {
                cleanup();
                update(temp);
            }

            updateEntities();

        }

        temp = API.readMemoryLong(viewAddressStatic);

        if (viewAddress != temp) {
            viewAddress = temp;
            boundsAddress = API.readMemoryLong(viewAddress + 208);
        }

        updateBounds();

    }

    private void update(long address) {

        mapAddress = address;

        internalWidth = API.readMemoryInt(address + 68);
        internalHeight = API.readMemoryInt(address + 72);
        entities.update(API.readMemoryLong(address + 40));
        System.out.println(API.readMemoryLong(address + 40));
        int tempId = API.readMemoryInt(address + 76);

        if (tempId != id) {
            id = tempId;
            main.hero.map = main.starManager.fromId(id);

            if (main.module instanceof MapChange) {
                ((MapChange) main.module).onMapChange();
            }
        }
    }

    private void updateBounds() {
        clientWidth = API.readMemoryInt(boundsAddress + 168);
        clientHeight = API.readMemoryInt(boundsAddress + 172);

        long updated = API.readMemoryLong(boundsAddress + 280);
        updated = API.readMemoryLong(updated + 112);

        boundX = API.readMemoryDouble(updated + 80);
        boundY = API.readMemoryDouble(updated + 88);
        boundMaxX = API.readMemoryDouble(updated + 112);
        boundMaxY = API.readMemoryDouble(updated + 120);
    }

    private void updateEntities() {

        entities.update();

        for (int i = 0; i < entities.size; i++) {

            long found = entities.elements[i];

            int id = API.readMemoryInt(found + 56);

            if (!ids.add(id)) continue;

            if (API.readMemoryInt(found + 112) == 3) {
                boxes.add(defs(new Box(id), found));
            } else if (id <= 150000499 && id >= 150000156) {
                portals.add(defs(main.starManager.fromIdPortal(id), found));
            } else if (id <= 150000950 && id >= 150000500) {
                battleStations.add(defs(new BattleStation(id), found));
            } else {

                int npc = API.readMemoryInt(found + 112);
                int visible = API.readMemoryInt(found + 116);
                int c = API.readMemoryInt(found + 120);
                int d = API.readMemoryInt(found + 124);

                if ((visible == 1 || visible == 0) && (c == 1 || c == 0) && d == 0) {
                    if (npc == 1) {
                        npcs.add(defs(new Npc(id), found));
                    } else if (npc == 0 && found != main.hero.address && found != main.hero.pet.address) {
                        ships.add(defs(new Ship(id), found));
                    }
                } else {
                    unknown.add(defs(new Entity(id), found));
                }

            }

        }

        iterate(boxes);
        iterate(portals);
        iterate(npcs);
        iterate(ships);
        iterate(unknown);
        iterate(battleStations);
    }

    private <E extends Entity> E defs(E entity, long address) {
        entity.update(address);

        entity.clickable.setRadius(0);

        return entity;
    }

    private void iterate(List<? extends Entity> entities) {
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);

            if (entity.isInvalid()) {
                entities.remove(i);
                entity.removed = true;
                ids.remove(entity.id);
                i--;
            } else {
                entity.update();
            }
        }
    }

    private void cleanup() {
        ids.clear();

        boxes.clear();
        npcs.clear();
        portals.clear();
        ships.clear();
        unknown.clear();
        battleStations.clear();
    }

    public boolean isOutOfMap(double x, double y) {
        return x < 0 || y < 0 || x > internalWidth || y > internalHeight;
    }

    public Portal closestByType(int... types) {
        for (Portal portal : portals) {

            for (int type : types) {
                if (portal.type == type) {
                    return portal;
                }
            }
        }

        return null;
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
