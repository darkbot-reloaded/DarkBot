package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.def.Manager;
import com.github.manolo8.darkbot.core.def.MapChange;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.objects.ArrayDO;
import com.github.manolo8.darkbot.core.objects.Map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager {

    private long mapAddressStatic;
    private long viewAddressStatic;
    public static long mapAddress;
    private long viewAddress;
    private long boundsAddress;

    private long bonusBoxConfirm;
    private long cargoBoxConfirm;

    private Main main;
    private ArrayDO entities;

    private HashSet<Integer> ids;

    public List<Box> bonusBoxes;
    public List<Box> cargoBoxes;

    public List<Npc> npcs;
    public List<Portal> portals;
    public List<Ship> ships;

    public List<Box> unknownBoxes;
    public List<Entity> unknown;

    public static int id;
    public Map map;

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

        this.ids = new HashSet<>();

        this.entities = new ArrayDO(0);

        this.bonusBoxes = new ArrayList<>();
        this.cargoBoxes = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.portals = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.unknownBoxes = new ArrayList<>();
        this.unknown = new ArrayList<>();

        this.map = main.starManager.fromId(id);
    }


    @Override
    public void install(BotManager botManager) {
        mapAddressStatic = botManager.screenManagerAddress + 256;
        viewAddressStatic = mapAddressStatic - 40;

        bonusBoxConfirm = API.readMemoryLong(botManager.settingsAddress + 452);
        cargoBoxConfirm = API.readMemoryLong(botManager.settingsAddress + 460);
    }

    @Override
    public void stop() {
        ids.clear();
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
        int tempId = API.readMemoryInt(address + 76);

        if (tempId != id) {
            id = tempId;
            map = main.starManager.fromId(id);

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

            if (!ids.contains(id)) {

                ids.add(id);

                if (API.readMemoryInt(found + 112) == 3) {

                    long confirm = API.readMemoryLong(found + 144);

                    //Needs to find an way to search the box name

                    if (confirm == bonusBoxConfirm) {
                        bonusBoxes.add(new Box(found, id));
                    } else if (confirm == cargoBoxConfirm) {
                        cargoBoxes.add(new Box(found, id));
                    } else {
                        unknownBoxes.add(new Box(found, id));
                    }

                } else if (id <= 150000400 && id >= 150000156) {
                    Portal portal = main.starManager.fromIdPortal(id);

                    portal.update(found);
                    portal.update();

                    portals.add(portal);
                } else {
                    int npc = API.readMemoryInt(found + 112);
                    int visible = API.readMemoryInt(found + 116);
                    int c = API.readMemoryInt(found + 120);
                    int d = API.readMemoryInt(found + 124);

                    if ((visible == 1 || visible == 0) && (c == 1 || c == 0) && d == 0) {
                        if (npc == 1) {
                            npcs.add(new Npc(found, id));
                        } else if (npc == 0 && found != main.hero.address && found != main.hero.pet.address) {
                            ships.add(new Ship(found, id));
                        }
                    } else {
                        unknown.add(new Entity(found, id));
                    }
                }

            }
        }

        iterate(bonusBoxes);
        iterate(cargoBoxes);
        iterate(portals);
        iterate(npcs);
        iterate(ships);
        iterate(unknownBoxes);
        iterate(unknown);
    }

    private void iterate(List<? extends Entity> entities) {

        Iterator<? extends Entity> iterator = entities.iterator();

        while (iterator.hasNext()) {
            Entity entity = iterator.next();

            if (entity.isInvalid()) {
                iterator.remove();
                ids.remove(entity.getId());
            } else {
                entity.update();
            }
        }

    }

    private void cleanup() {
        ids.clear();
        bonusBoxes.clear();
        cargoBoxes.clear();
        npcs.clear();
        portals.clear();
        ships.clear();
        unknownBoxes.clear();
        unknown.clear();
    }

    public boolean isOutOfMap(double x, double y) {
        return x < 0 || y < 0 || x > internalWidth || y > internalHeight;
    }

    public boolean canClick(double x, double y) {

        for (Npc npc : npcs) {
            if (npc.location.distance(x, y) < 300) {
                return false;
            }
        }

        return true;
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
                ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }

    public void translateMousePress(double x, double y) {
        API.mousePress(
                ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }

    public void translateMouseClick(double x, double y) {
        API.mouseClick(
                ((x - boundX) / (boundMaxX - boundX) * clientWidth),
                ((y - boundY) / (boundMaxY - boundY) * clientHeight)
        );
    }
}
