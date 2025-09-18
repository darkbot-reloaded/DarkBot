package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.ConfigEntity;
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
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.factory.EntityFactory;
import com.github.manolo8.darkbot.core.utils.factory.EntityRegistry;
import eu.darkbot.api.game.entities.FakeEntity;
import eu.darkbot.api.game.entities.Mist;
import eu.darkbot.api.game.entities.Station;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.util.Timer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BARRIER;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BASE_HANGAR;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BASE_STATION;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BASE_TURRET;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BOX;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BUFF_CAPSULE;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BURNING_TRAIL;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.BURNING_TRAIL_ENEMY;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.CBS_ASTEROID;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.CBS_CONSTRUCTION;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.CBS_MODULE;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.CBS_MODULE_CON;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.CBS_STATION;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.HEADQUARTER;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.LOW_RELAY;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.MINE;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.MIST_ZONE;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.NPC;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.ORE;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PET;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PET_BEACON;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PLAYER;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PLUTUS_GENERATOR;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PLUTUS_GENERATOR_GREEN;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PLUTUS_GENERATOR_RED;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.POD_HEAL;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.PORTAL;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.QUEST_GIVER;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.REFINERY;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.REPAIR_STATION;
import static com.github.manolo8.darkbot.core.utils.factory.EntityFactory.SPACE_BALL;

public class EntityList extends Updatable implements EntitiesAPI {

    public final List<Entity> all = new ArrayList<>();
    public final List<Entity> unknown = new ArrayList<>();
    public final List<Obstacle> obstacles = new ArrayList<>();
    public final List<List<? extends Entity>> allEntities = new ArrayList<>();

    public final EntityRegistry entityRegistry;

    public final List<Barrier> barriers;
    public final List<NoCloack> noCloack;
    public final List<Box> boxes;
    public final List<Mine> mines;
    public final List<Npc> npcs;
    public final List<Portal> portals;
    public final List<Ship> ships;
    public final List<Player> players;
    public final List<Pet> pets;
    public final List<BattleStation> battleStations;
    public final List<BasePoint> basePoints;

    public final FakeNpc fakeNpc;

    private final List<Relay> relays;
    private final List<SpaceBall> spaceBalls;
    private final List<StaticEntity> staticEntities;

    private final Main main;
    private final EventBrokerAPI eventBroker;

    private final IntSet ids = new IntOpenHashSet();
    private final FlashListLong entitiesArr = FlashListLong.ofVector().noAuto();

    private final Timer lastLocatorMatch = Timer.get(5_000);
    private Location lastLocatorLocation = new Location();

    public EntityList(Main main, EventBrokerAPI eventBroker) {
        this.main = main;
        this.fakeNpc = new FakeNpc(main);
        this.eventBroker = eventBroker;

        this.allEntities.add(unknown);
        this.entityRegistry = new EntityRegistry(main, this::onEntityCreate, unknown::add);

        this.barriers       = register(BARRIER);
        this.noCloack       = register(MIST_ZONE);
        this.boxes          = register(BOX, ORE);
        this.mines          = register(MINE);
        this.npcs           = register(NPC, LOW_RELAY);
        this.portals        = register(PORTAL);
        this.ships          = register(PLAYER, PET);
        this.players        = register(PLAYER);
        this.pets           = register(PET);
        this.battleStations = register(CBS_ASTEROID, CBS_MODULE, CBS_STATION, CBS_MODULE_CON, CBS_CONSTRUCTION);
        this.basePoints     = register(BASE_HANGAR, BASE_STATION, HEADQUARTER, QUEST_GIVER, BASE_TURRET, REPAIR_STATION, REFINERY);
        this.relays         = register(LOW_RELAY);
        this.spaceBalls     = register(SPACE_BALL);
        this.staticEntities = register(POD_HEAL, BUFF_CAPSULE,
                BURNING_TRAIL, BURNING_TRAIL_ENEMY, PLUTUS_GENERATOR, PLUTUS_GENERATOR_RED, PLUTUS_GENERATOR_GREEN, PET_BEACON);
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
        this.entitiesArr.update(API.readLong(address + 40));
    }

    private void onEntityCreate(Entity entity) {
        this.all.add(entity);
        if (entity instanceof Obstacle)
            this.obstacles.add((Obstacle) entity);

        this.eventBroker.sendEvent(new EntityCreateEvent(entity));
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
        for (int i = 0; i < entitiesArr.size(); i++) {
            long entityPtr = entitiesArr.getLong(i);
            int id = API.readInt(entityPtr + 56);
            if (ids.add(id))
                entityRegistry.sendEntity(id, entityPtr);
        }
    }

    private void removeAllInvalidEntities() {
        main.hero.pet.removed = main.hero.pet.isInvalid(address);

        for (Iterator<Entity> it = all.iterator(); it.hasNext(); ) {
            Entity entity = it.next();

            if (entity.isInvalid(address) || entity.address == main.hero.address || entity.address == main.hero.pet.address) {
                it.remove();
                entity.removed();
                ids.remove(entity.id);
                eventBroker.sendEvent(new EntityRemoveEvent(entity));
            } else entity.update();
        }

        this.obstacles.removeIf(Obstacle::isRemoved);
        for (List<? extends Entity> entities : allEntities)
            entities.removeIf(e -> !e.isValid());
    }

    public void clear() {
        synchronized (Main.UPDATE_LOCKER) {
            ids.clear();
            entityRegistry.clearCache();

            obstacles.clear();
            fakeNpc.removed();

            for (Entity entity : all) {
                entity.removed();
                eventBroker.sendEvent(new EntityRemoveEvent(entity));
            }
            all.clear();

            for (List<? extends Entity> entities : allEntities) {
                entities.clear();
            }
        }
    }

    public void updatePing(Location location, NpcInfo info) {
        fakeNpc.set(location, info);
        boolean shouldBeNpc = info != null && fakeNpc.isPingAlive() &&
                npcs.stream().noneMatch(n -> fakeNpc != n && n.npcInfo == info && n.locationInfo.distance(fakeNpc) < 600);

        if (!shouldBeNpc) {
            if (!Objects.equals(lastLocatorLocation, location))
                lastLocatorLocation = location == null ? null : location.copy();

            lastLocatorMatch.activate();
            npcs.remove(fakeNpc);
        } else {
            if (location == null || (lastLocatorLocation != null
                    && lastLocatorLocation.distance(location) < 100 && lastLocatorMatch.isActive()))
                return;

            if (!npcs.contains(fakeNpc)) npcs.add(fakeNpc);
        }
    }

    public @Nullable Entity findEntityByAddress(long entityAddress) {
        if (main.hero.address == entityAddress) return main.hero;
        if (main.hero.pet.address == entityAddress) return main.hero.pet;

        for (Entity entity : all) {
            if (entity.address == entityAddress) {
                return entity;
            }
        }
        return null;
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
        return Collections.unmodifiableList(all);
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

    @Override
    public FakeEntity.Builder fakeEntityBuilder() {
        return new FakeEntity.Builder.Impl() {
            private <T extends Entity & FakeEntity> T addEntity(T entity, Collection<? super T> collection) {
                apply(entity);
                collection.add(entity);
                onEntityCreate(entity);
                return entity;
            }

            @Override
            public FakeEntity.FakeMine mine(int typeId) {
                return addEntity(apply(new Mine.Fake(typeId)), mines);
            }

            @Override
            public FakeEntity.FakeNpc npc(eu.darkbot.api.config.types.NpcInfo npcInfo) {
                return addEntity(apply(new Npc.Fake((NpcInfo) npcInfo)), npcs);
            }

            @Override
            public FakeEntity.FakeNpc npc(String npcName) {
                return npc(ConfigEntity.INSTANCE.getOrCreateNpcInfo(npcName));
            }

            @Override
            public FakeEntity.FakeBox box(eu.darkbot.api.config.types.BoxInfo box) {
                return addEntity(apply(new Box.Fake((BoxInfo) box)), boxes);
            }

            @Override
            public FakeEntity.FakeBox box(String boxName) {
                return box(ConfigEntity.INSTANCE.getOrCreateBoxInfo(boxName));
            }
        };
    }

}
