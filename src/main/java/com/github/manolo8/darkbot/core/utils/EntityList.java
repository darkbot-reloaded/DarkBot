package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.entities.Barrier;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.FakeNpc;
import com.github.manolo8.darkbot.core.entities.Mine;
import com.github.manolo8.darkbot.core.entities.NoCloack;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.factory.EntityFactory;
import com.github.manolo8.darkbot.core.utils.factory.EntityRegistry;
import eu.darkbot.api.entities.Station;
import eu.darkbot.api.managers.EntitiesAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.*;

public class EntityList extends Updatable implements EntitiesAPI {
    public final EntityRegistry entityRegistry = new EntityRegistry();

    public final List<Obstacle> obstacles                 = new ArrayList<>();
    public final List<List<? extends Entity>> allEntities = new ArrayList<>();

    public final List<Barrier> barriers             = register(BARRIER);
    public final List<NoCloack> noCloack            = register(MIST_ZONE);
    public final List<Box> boxes                    = register(BOX, ORE);
    public final List<Mine> mines                   = register(MINE);
    public final List<Npc> npcs                     = register(NPC, LOW_RELAY);
    public final List<Portal> portals               = register(PORTAL);
    public final List<Ship> ships                   = register(PLAYER, PET);
    public final List<Pet> pets                     = register(PET);
    public final List<BattleStation> battleStations = register(CBS_ASTEROID, CBS_MODULE, CBS_STATION, CBS_MODULE_CON, CBS_CONSTRUCTION);
    public final List<BattleStation.Module> modules = register(CBS_MODULE, CBS_MODULE_CON);
    public final List<BasePoint> basePoints         = register(BASE_HANGAR, BASE_STATION, HEADQUARTER, QUEST_GIVER, BASE_TURRET, REPAIR_STATION, REFINERY);
    public final List<Entity> unknown               = register();
    public final FakeNpc fakeNpc;

    private final Main main;
    private final Set<Integer> ids = new HashSet<>();
    private final ObjArray entitiesArr = ObjArray.ofVector();

    public EntityList(Main main) {
        this.main    = main;
        this.fakeNpc = new FakeNpc(main);
        this.entityRegistry.setMain(main);

        this.entityRegistry.addToAll(entity -> {
            if (entity instanceof Obstacle) obstacles.add((Obstacle) entity);
        });
        this.entityRegistry.addDefault(unknown::add);

        this.main.status.add(this::refreshRadius);
    }

    @Override
    public void update() {
        synchronized (Main.UPDATE_LOCKER) {
            removeAllInvalidEntities();
            refreshEntities();
            updatePing(main.mapManager.pingLocation, main.guiManager.pet.getTrackedNpc());
        }
    }

    @Override
    public void update(long address) {
        super.update(address);
        this.clear();
        this.entitiesArr.update(API.readMemoryLong(address + 40));
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> List<T> register(EntityFactory... types) {
        List<T> list = new ArrayList<>();
        this.allEntities.add(list);

        for (EntityFactory type : types)
            this.entityRegistry.add(type, e -> list.add((T) e));

        return list;
    }

    private void refreshEntities() {
        entitiesArr.update();
        for (int i = 0; i < entitiesArr.getSize(); i++) {
            long entityPtr = entitiesArr.get(i);

            int id = API.readMemoryInt(entityPtr + 56);
            if (ids.add(id)) entityRegistry.sendEntity(id, entityPtr);
        }
    }

    private void removeAllInvalidEntities() {
        main.hero.pet.removed = main.hero.pet.isInvalid(address);

        for (List<? extends Entity> entities : allEntities) {
            // Remove invalid entities and update valid ones
            entities.removeIf(entity -> {
                if (entity.isInvalid(address) || entity.address == main.hero.address || entity.address == main.hero.pet.address) {
                    ids.remove(entity.id);
                    entity.removed();
                    return true;
                } 
                entity.update();
                return false;
            });
        }

        this.obstacles.removeIf(Obstacle::isRemoved);
    }

    public void updatePing(Location location, NpcInfo info) {
        fakeNpc.set(location, info);
        boolean shouldBeNpc = location != null && info != null && fakeNpc.isPingAlive() &&
                npcs.stream().noneMatch(n -> fakeNpc != n && n.npcInfo == info && n.locationInfo.distance(location) < 500);

        if (!shouldBeNpc) npcs.remove(fakeNpc);
        else if (!npcs.contains(fakeNpc)) npcs.add(fakeNpc);
    }

    private void doInEachEntity(Consumer<Entity> consumer) {
        allEntities.forEach(entities -> entities.forEach(consumer));
    }

    public void clear() {
        synchronized (Main.UPDATE_LOCKER) {
            ids.clear();
            entityRegistry.clearCache();

            obstacles.clear();
            fakeNpc.removed();

            allEntities.forEach(entities -> {
                entities.forEach(Entity::removed);
                entities.clear();
            });
        }
    }

    private void refreshRadius(boolean value) {
        synchronized (Main.UPDATE_LOCKER) {
            if (value) doInEachEntity(entity -> entity.clickable.setRadius(0));
            else doInEachEntity(entity -> entity.clickable.reset());
        }
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Npc> getNpcs() {
        return npcs;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Pet> getPets() {
        return pets;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Ship> getPlayers() {
        return ships;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Box> getBoxes() {
        return boxes;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Mine> getMines() {
        return mines;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Portal> getPortals() {
        return portals;
    }

    @Override
    public Collection<? extends Station> getStations() {
        return basePoints;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.BattleStation> getBattleStations() {
        return battleStations;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.BattleStation.Module> getBaseModules() {
        return modules;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Entity> getAll() {
        return allEntities.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.Entity> getUnknown() {
        return unknown;
    }

    @Override
    public Collection<? extends eu.darkbot.api.objects.Obstacle> getObstacles() {
        return obstacles;
    }
}
