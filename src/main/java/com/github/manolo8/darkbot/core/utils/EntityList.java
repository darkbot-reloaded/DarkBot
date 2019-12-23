package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.entities.*;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.LocationInfo;
import com.github.manolo8.darkbot.core.objects.swf.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.github.manolo8.darkbot.Main.API;
import static java.lang.Thread.activeCount;

public class EntityList extends Updatable {

    private final Main main;
    private final Array entitiesAddress;
    public final List<List<? extends Entity>> allEntities;
    private final Set<Integer> ids;

    public final List<Obstacle> obstacles;

    public final List<Barrier> barriers;
    public final List<NoCloack> noCloack;
    public final List<Box> boxes;
    public final List<Npc> npcs;
    public final List<Portal> portals;
    public final List<Ship> ships;
    public final List<BattleStation> battleStations;
    public final List<BasePoint> basePoints;
    public final List<Entity> unknown;

    public EntityList(Main main) {
        this.main = main;

        this.entitiesAddress = new Array(0);
        this.allEntities = new ArrayList<>();

        this.ids = new HashSet<>();

        this.obstacles = new ArrayList<>();

        this.barriers = new ArrayList<>();
        this.noCloack = new ArrayList<>();
        this.boxes = new ArrayList<>();
        this.npcs = new ArrayList<>();
        this.portals = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.battleStations = new ArrayList<>();
        this.basePoints = new ArrayList<>();
        this.unknown = new ArrayList<>();

        this.allEntities.add(barriers);
        this.allEntities.add(noCloack);
        this.allEntities.add(boxes);
        this.allEntities.add(npcs);
        this.allEntities.add(portals);
        this.allEntities.add(ships);
        this.allEntities.add(battleStations);
        this.allEntities.add(basePoints);
        this.allEntities.add(unknown);

        this.main.status.add(this::refreshRadius);
    }

    @Override
    public void update() {

        synchronized (Main.UPDATE_LOCKER) {
            removeAllInvalidEntities();

            refreshEntities();
        }

    }

    @Override
    public void update(long address) {
        super.update(address);

        entitiesAddress.update(API.readMemoryLong(address + 40));

        clear();
    }

    private void refreshEntities() {

        entitiesAddress.update();
        for (int i = 0; i < entitiesAddress.size; i++) {
            long found = entitiesAddress.elements[i];

            int id = API.readMemoryInt(found + 56);
            if (!ids.add(id)) continue;

            int rnd = API.readMemoryInt(found + 112);
            int hullId = API.readMemoryInt(found + 116);
            String key = API.readMemoryString(API.readMemoryLong(found + 136));

            if (key.equals("NOA")) {
                barriers.add(whenAdd(new Barrier(id), found));
            } else if (key.equals("DMG")) {
                noCloack.add(whenAdd(new NoCloack(id), found));
            } else if (id < 0 && rnd == 3) {
                boxes.add(whenAdd(new Box(id), found));
            } else if (150000156 <= id && id <= 150000514) {
                LocationInfo loc = new LocationInfo(API.readMemoryLong(found + 64));
                loc.update();
                portals.add(whenAdd(main.starManager.getOrCreate(id, rnd, (int) loc.now.x, (int) loc.now.y), found));
            } else if (150000532 <= id && id <= 150000950 && hullId < 255 && hullId >= 0) {
                battleStations.add(whenAdd(new BattleStation(id, hullId), found));
            } else if (id <= 150000147 && id >= 150000000) {
                // 1-1: 000-022  1-4: 023
                // 2-1: 024-046  2-4: 047
                // 3-1: 048-070  3-4: 071
                // 1-5: 072      1-8: 073-095
                // 2-5: 096      2-8: 097-119
                // 3-5: 120      3-8: 121-143
                // 1-BL: 144  2-BL: 145  3-BL: 146
                // 5-2: 147
                this.basePoints.add(whenAdd(new BasePoint(id), found));
            } else {
                int npc = API.readMemoryInt(found + 112);
                int visible = API.readMemoryInt(found + 116);
                int c = API.readMemoryInt(found + 120);
                int d = API.readMemoryInt(found + 124);

                if (id > 0 && (visible == 1 || visible == 0) && (c == 1 || c == 0) && d == 0) {
                    if (npc == 1) {
                        npcs.add(whenAdd(new Npc(id), found));
                    } else if (npc == 0 && found != main.hero.address && found != main.hero.pet.address) {
                        ships.add(whenAdd(new Ship(id), found));
                    }
                } else if (id >= 100000101 && id <= 100000104) {
                    this.npcs.add(whenAdd(new MapNpc(id), found)); // LoW map relays
                } else {
                    Entity entity = whenAdd(new Entity(id), found);
                    if (entity.clickable.defRadius == 150) {
                        npcs.add(whenAdd(new MapNpc(id), found)); // Experiment zone beacons
                    } else {
                        unknown.add(entity);
                    }
                }
            }
        }

        /*if (main.config.BOT_SETTINGS.DEV_STUFF) {
            long[] addr = Arrays.copyOf(entitiesAddress.elements, entitiesAddress.size);
            Arrays.sort(addr);

            for (int i = 0; i < addr.length; i++) {
                long curr = addr[i];
                if (!newFound.contains(curr)) continue;
                int id = API.readMemoryInt(curr + 56);
                if (id < 150_000_000 || id >= 150_200_000) continue;

                StringBuilder str = new StringBuilder().append(id);
                int search = 200;
                if (i < addr.length - 1) search = Math.min(search, Math.max(100, (int) (addr[i+1] - curr)));
                for (int offset = 0; offset < search; offset += 4) {
                    int f = API.readMemoryInt(curr + offset);
                    if (f > -1000 && f < 1000) str.append(" | ").append(offset).append(":").append(f);
                }
                System.out.println(str);
            }
        }*/

    }

    private <E extends Entity> E whenAdd(E entity, long address) {

        entity.added();
        entity.update(address);
        entity.update();

        if (entity instanceof Obstacle)
            obstacles.add((Obstacle) entity);

        if (main.isRunning())
            entity.clickable.setRadius(0);

        return entity;
    }

    private void whenRemove(Entity entity) {
        entity.removed();
    }

    private void removeAllInvalidEntities() {
        main.hero.pet.removed = main.hero.pet.isInvalid(address);

        for (List<? extends Entity> entities : allEntities) {
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);

                if (entity.isInvalid(address) ||
                        entity.address == main.hero.address || entity.address == main.hero.pet.address) {
                    entities.remove(i);
                    ids.remove(entity.id);
                    whenRemove(entity);
                    i--;
                } else {
                    entity.update();
                }
            }
        }

        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.isRemoved()) {
                obstacles.remove(i);
                i--;
            }
        }
    }

    private void doInEachEntity(Consumer<Entity> consumer) {
        for (List<? extends Entity> entities : allEntities) {
            entities.forEach(consumer);
        }
    }

    private void clear() {
        synchronized (Main.UPDATE_LOCKER) {
            ids.clear();

            obstacles.clear();

            for (List<? extends Entity> entities : allEntities) {
                for (Entity entity : entities) entity.removed();
                entities.clear();
            }
        }
    }

    private void refreshRadius(boolean value) {
        synchronized (Main.UPDATE_LOCKER) {
            if (value) doInEachEntity(entity -> entity.clickable.setRadius(0));
            else doInEachEntity(entity -> entity.clickable.reset());
        }
    }
}
