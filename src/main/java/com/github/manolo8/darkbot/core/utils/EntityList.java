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
import com.github.manolo8.darkbot.core.entities.Player;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Relay;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.entities.SpaceBall;
import com.github.manolo8.darkbot.core.entities.StaticEntity;
import com.github.manolo8.darkbot.core.itf.Obstacle;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.factory.EntityFactory;
import com.github.manolo8.darkbot.core.utils.factory.EntityRegistry;
import eu.darkbot.api.game.entities.Mist;
import eu.darkbot.api.game.entities.Station;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.util.Timer;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    public final List<Player> players               = register(PLAYER);
    public final List<Pet> pets                     = register(PET);
    public final List<BattleStation> battleStations = register(CBS_ASTEROID, CBS_MODULE, CBS_STATION, CBS_MODULE_CON, CBS_CONSTRUCTION);
    public final List<BasePoint> basePoints         = register(BASE_HANGAR, BASE_STATION, HEADQUARTER, QUEST_GIVER, BASE_TURRET, REPAIR_STATION, REFINERY);
    public final List<Entity> unknown               = register();
    public final FakeNpc fakeNpc;

    private final List<Relay> relays = register(LOW_RELAY);
    private final List<SpaceBall> spaceBalls = register(SPACE_BALL);
    private final List<StaticEntity> staticEntities = register(POD_HEAL, BUFF_CAPSULE, BURNING_TRAIL, PLUTUS_GENERATOR);

    private final Main main;
    private final EventBrokerAPI eventBroker;
    private final Set<Integer> ids = new HashSet<>();
    private final ObjArray entitiesArr = ObjArray.ofVector();

    public EntityList(Main main, EventBrokerAPI eventBroker) {
        this.main    = main;
        this.fakeNpc = new FakeNpc(main);
        this.eventBroker = eventBroker;
        this.entityRegistry.setMain(main);

        this.entityRegistry.addToAll(entity -> {
            if (entity instanceof Obstacle) obstacles.add((Obstacle) entity);
            eventBroker.sendEvent(new EntityCreateEvent(entity));
        });
        this.entityRegistry.addDefault(e -> {
            unknown.add(e);
            eventBroker.sendEvent(new EntityCreateEvent(e));
        });

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
            if (ids.add(id))
                entityRegistry.sendEntity(id, entityPtr);
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

                    if (entities != ships)
                        eventBroker.sendEvent(new EntityRemoveEvent(entity));
                    return true;
                } 
                entity.update();
                return false;
            });
        }

        this.obstacles.removeIf(Obstacle::isRemoved);
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

                if (entities != ships)
                    entities.forEach(e -> eventBroker.sendEvent(new EntityRemoveEvent(e)));
                entities.clear();
            });
        }
    }

    private void refreshRadius(boolean running) {
        synchronized (Main.UPDATE_LOCKER) {
            main.hero.pet.clickable.toggle(!running);
            doInEachEntity(entity -> entity.clickable.toggle(!running));
        }
    }

    private Location lastLocatorLocation = new Location();
    private final Timer lastLocatorMatch = Timer.get(5_000);

    public void updatePing(Location location, NpcInfo info) {
        fakeNpc.set(location, info);
        boolean shouldBeNpc = info != null && fakeNpc.isPingAlive() &&
                npcs.stream().noneMatch(n -> fakeNpc != n && n.npcInfo == info && n.locationInfo.distance(fakeNpc) < 600);

        if (!shouldBeNpc) {
            if (!Objects.equals(lastLocatorLocation, location))
                lastLocatorLocation = location == null ? null : location.copy();

            lastLocatorMatch.activate();
            npcs.remove(fakeNpc);
        }
        else {
            if (location == null || (lastLocatorLocation != null
                    && lastLocatorLocation.distance(location) < 100 && lastLocatorMatch.isActive()))
                return;
            
            if (!npcs.contains(fakeNpc)) npcs.add(fakeNpc);
        }
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Npc> getNpcs() {
        return Collections.unmodifiableList(npcs);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Pet> getPets() {
        return Collections.unmodifiableList(pets);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Ship> getShips() {
        return Collections.unmodifiableList(ships);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Mine> getMines() {
        return Collections.unmodifiableList(mines);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Portal> getPortals() {
        return Collections.unmodifiableList(portals);
    }

    @Override
    public Collection<? extends Station> getStations() {
        return Collections.unmodifiableList(basePoints);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.BattleStation> getBattleStations() {
        return Collections.unmodifiableList(battleStations);
    }

    @Override
    public @UnmodifiableView Collection<? extends eu.darkbot.api.game.entities.Relay> getRelays() {
        return Collections.unmodifiableList(relays);
    }

    @Override
    public @UnmodifiableView Collection<? extends eu.darkbot.api.game.entities.SpaceBall> getSpaceBalls() {
        return Collections.unmodifiableList(spaceBalls);
    }

    @Override
    public @UnmodifiableView Collection<? extends eu.darkbot.api.game.entities.StaticEntity> getStaticEntities() {
        return Collections.unmodifiableList(staticEntities);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Entity> getAll() {
        return allEntities.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.entities.Entity> getUnknown() {
        return Collections.unmodifiableList(unknown);
    }

    @Override
    public Collection<? extends eu.darkbot.api.game.other.Obstacle> getObstacles() {
        return Collections.unmodifiableList(obstacles);
    }

    @Override
    public @UnmodifiableView Collection<? extends Mist> getMists() {
        return Collections.unmodifiableList(noCloack);
    }

    @Override
    public @UnmodifiableView Collection<? extends eu.darkbot.api.game.entities.Barrier> getBarriers() {
        return Collections.unmodifiableList(barriers);
    }
}
