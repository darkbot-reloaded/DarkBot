package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import static com.github.manolo8.darkbot.Main.API;
import static com.github.manolo8.darkbot.behaviours.memory.AvmMemory.OBJECT_VTABLE;

/**
 * Releases ships that left the map.
 * <p>
 * The client keeps every removed ship until the next map change: the ship's HUD view, its info and top displays
 * keep pointing to the ship, and the ship's traits point back to it as their owner. Each of these references alone
 * keeps the ship reachable.
 * <p>
 * Ships are followed through the HUD layer of ship views: its trait list holds the info trait of every ship the HUD
 * shows, and its view dictionary maps each trait to the ship's view. A followed ship counts as removed when the HUD
 * and the ship itself agree: its trait left the layer's list, and its container has been null for
 * {@link #REMOVED_CHECKS} checks in a row. Then those references are set to null.
 */
class ShipReferenceCleaner implements LeakCleaner {

    private static final String SHIP_VIEW_CLASS = "ShipHUD$";
    private static final String SCREEN_MANAGER_MAP_VIEW = "_mapView";
    private static final String MAP_VIEW_HUD = "_hud";
    private static final String HUD_LAYERS = "_traitClass2internalList";
    private static final String LAYER_CONTAINER = "_container";
    private static final String LAYER_VIEW_CLASS = "_viewClass";
    private static final String LAYER_TRAIT_LIST = "_list";
    private static final String LAYER_VIEWS = "_viewByData";
    private static final String TRAIT_LIST_TRAITS = "_traits";
    private static final String SHIP_HUD_ENTITY = "_entity";
    private static final String SHIP_HUD_INFO = "_shipInfoDisplay";
    private static final String SHIP_HUD_TOP = "_topDisplay";
    private static final String DISPLAY_OWNER = "_owner";
    private static final String INFO_TRAIT = "_trait";
    private static final String ENTITY_ID = "_id";
    private static final String ENTITY_CONTAINER = "_container";
    private static final String ENTITY_TRAITS = "_traits";
    private static final String TRAIT_OWNER = "_owner";

    private static final long INTERVAL_MS = 1_000;
    private static final int REMOVED_CHECKS = 2;
    /** Checks in a row without the ship layer while the bot sees ships, before it is reported as not found. */
    private static final int LAYER_MISSES_REPORTED = 60;
    /** Far above the layers of a HUD, the traits of a ship and the ships of a map. */
    private static final int MAX_LAYERS = 64, MAX_TRAITS = 4096;

    private final Main main;
    private final BotInstaller botInstaller;
    private final SlotResolver slots;

    private final ListReader layers = new ListReader(ListReader.Layout.VECTOR, MAX_LAYERS);
    private final ListReader layerTraits = new ListReader(ListReader.Layout.VECTOR, MAX_TRAITS);
    private final ListReader shipTraits = new ListReader(ListReader.Layout.VECTOR, MAX_TRAITS);
    private final DictionaryReader views = new DictionaryReader();
    private final LongSet liveTraits = new LongOpenHashSet();
    private final Long2ObjectMap<TrackedShip> tracked = new Long2ObjectOpenHashMap<>();

    private long hud, shipLayer, shipViewClass;
    private int mapId = Integer.MIN_VALUE;
    private int layerMisses;
    private boolean layerMissReported;

    ShipReferenceCleaner(Main main, BotInstaller botInstaller, SlotResolver slots) {
        this.main = main;
        this.botInstaller = botInstaller;
        this.slots = slots;
    }

    @Override
    public long getIntervalMs() {
        return INTERVAL_MS;
    }

    /** Follows the ships the HUD shows and releases the removed ones. Does nothing while the HUD can't be read. */
    @Override
    public void clean() {
        if (!followShipLayer() || !readLiveTraits()) return;
        trackNewShips();
        releaseRemovedShips();
    }

    @Override
    public void reset() {
        tracked.clear();
        liveTraits.clear();
        hud = shipLayer = shipViewClass = 0;
        mapId = Integer.MIN_VALUE;
        layerMisses = 0;
        layers.release();
        layerTraits.release();
        shipTraits.release();
        views.release();
    }

    /**
     * Finds the ship layer of the current HUD, or checks that the one found before still belongs to it.
     * A new map or HUD means a new set of ships: followed ships are forgotten.
     */
    private boolean followShipLayer() {
        int currentMap = main.hero.map != null ? main.hero.map.id : Integer.MIN_VALUE;
        if (currentMap != mapId) {
            tracked.clear();
            mapId = currentMap;
        }

        long currentHud = findHud();
        if (currentHud == 0) return false;
        if (currentHud != hud) {
            tracked.clear();
            hud = currentHud;
            shipLayer = 0;
        }
        if (!isShipLayer(shipLayer)) shipLayer = findShipLayer();
        if (shipLayer == 0) {
            reportMissingShipLayer();
            return false;
        }
        layerMisses = 0;
        return true;
    }

    private long findHud() {
        Long screenManager = botInstaller.screenManagerAddress.get();
        if (screenManager == null || screenManager == 0) return 0;
        return slots.readRequired(slots.readRequired(screenManager, SCREEN_MANAGER_MAP_VIEW), MAP_VIEW_HUD);
    }

    /** Whether the layer is still the ship layer found in the current HUD. */
    private boolean isShipLayer(long layer) {
        return layer != 0 && slots.readPointer(layer, LAYER_CONTAINER) == hud
                && slots.readPointer(layer, LAYER_VIEW_CLASS) == shipViewClass;
    }

    /** The layer of the current HUD that shows ship views, recognized by its view class rather than by index. */
    private long findShipLayer() {
        if (!layers.read(slots.readRequired(hud, HUD_LAYERS))) return 0;
        for (int i = 0; i < layers.count(); i++) {
            long layer = layers.get(i);
            long viewClass = slots.readRequired(layer, LAYER_VIEW_CLASS);
            if (viewClass == 0 || !SHIP_VIEW_CLASS.equals(ByteUtils.readObjectName(viewClass))) continue;
            if (slots.readRequired(layer, LAYER_CONTAINER) != hud) continue;
            shipViewClass = viewClass;
            return layer;
        }
        return 0;
    }

    /** Reads the traits of the ships the HUD shows. False if they can't be read: then no ship is released. */
    private boolean readLiveTraits() {
        liveTraits.clear();
        long traitList = slots.readRequired(shipLayer, LAYER_TRAIT_LIST);
        if (!layerTraits.read(slots.readRequired(traitList, TRAIT_LIST_TRAITS))) return false;
        for (int i = 0; i < layerTraits.count(); i++) {
            long trait = layerTraits.get(i);
            if (trait != 0) liveTraits.add(trait);
        }
        return true;
    }

    /** Starts following the ships shown by the HUD that are not followed yet. */
    private void trackNewShips() {
        if (!views.read(slots.readRequired(shipLayer, LAYER_VIEWS))) return;
        for (int i = 0; i < views.size(); i++) {
            long trait = views.key(i), view = views.value(i);
            // Views of traits no longer listed belong to ships already removed: only listed ones are followed
            if (!liveTraits.contains(trait)) continue;
            long ship = slots.readPointer(view, SHIP_HUD_ENTITY);
            if (ship == 0 || tracked.containsKey(ship) || isHero(ship)) continue;

            TrackedShip t = track(ship, trait, view);
            if (t != null) tracked.put(ship, t);
        }
    }

    private TrackedShip track(long ship, long trait, long view) {
        if (!slots.require(ship, ENTITY_ID, ENTITY_CONTAINER, ENTITY_TRAITS)) return null;
        long idOffset = slots.offset(ship, ENTITY_ID);
        long containerOffset = slots.offset(ship, ENTITY_CONTAINER);
        long traitsOffset = slots.offset(ship, ENTITY_TRAITS);
        // A listed ship is on the map and owns its trait. Otherwise an offset is wrong, which could make live
        // ships look removed: such ships are never followed.
        if (API.readAtom(ship + containerOffset) == 0 || slots.readPointer(trait, TRAIT_OWNER) != ship) return null;

        return new TrackedShip(trait, view, API.readLong(view + OBJECT_VTABLE), API.readLong(ship + OBJECT_VTABLE),
                API.readInt(ship + idOffset), idOffset, containerOffset, traitsOffset);
    }

    private void releaseRemovedShips() {
        ObjectIterator<Long2ObjectMap.Entry<TrackedShip>> it = tracked.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<TrackedShip> entry = it.next();
            long ship = entry.getLongKey();
            TrackedShip t = entry.getValue();

            // Memory reused by another object
            if (API.readLong(ship + OBJECT_VTABLE) != t.vtable || API.readInt(ship + t.idOffset) != t.id) {
                it.remove();
                continue;
            }
            if (liveTraits.contains(t.trait) || API.readAtom(ship + t.containerOffset) != 0) {
                t.removedChecks = 0;
                continue;
            }
            if (++t.removedChecks < REMOVED_CHECKS) continue;

            release(ship, t);
            it.remove();
        }
    }

    private void release(long ship, TrackedShip t) {
        // The view is only touched while it is still the same object that was followed
        boolean sameView = API.readLong(t.view + OBJECT_VTABLE) == t.viewVtable;
        if (sameView && slots.requireReference(t.view, SHIP_HUD_ENTITY)
                && slots.readPointer(t.view, SHIP_HUD_ENTITY) == ship) {
            long info = slots.readPointer(t.view, SHIP_HUD_INFO);
            if (slots.requireReference(info, DISPLAY_OWNER, INFO_TRAIT)
                    && slots.readPointer(info, DISPLAY_OWNER) == ship) {
                long trait = slots.readPointer(info, INFO_TRAIT);
                if (trait != 0 && slots.readPointer(trait, TRAIT_OWNER) == ship) clear(info, INFO_TRAIT, trait);
                clear(info, DISPLAY_OWNER, ship);
            }
            long top = slots.readPointer(t.view, SHIP_HUD_TOP);
            if (slots.requireReference(top, DISPLAY_OWNER)) clear(top, DISPLAY_OWNER, ship);
            clear(t.view, SHIP_HUD_ENTITY, ship);
        }

        if (!shipTraits.read(API.readAtom(ship + t.traitsOffset))) return;
        for (int i = 0; i < shipTraits.count(); i++) clear(shipTraits.get(i), TRAIT_OWNER, ship);
    }

    /** Sets object.field to null if it still points to the expected object. */
    private void clear(long object, String field, long expected) {
        long offset = slots.referenceOffset(object, field);
        if (offset >= 0 && API.readAtom(object + offset) == expected) API.writeLong(object + offset, 0);
    }

    /**
     * The hero and every ship the bot sees have a HUD view, so a ship layer that stays missing while there are ships
     * means the HUD changed. Reported once.
     */
    private void reportMissingShipLayer() {
        if (main.mapManager.entities.ships.isEmpty() && main.mapManager.entities.npcs.isEmpty()) return;
        if (layerMissReported || ++layerMisses < LAYER_MISSES_REPORTED) return;
        layerMissReported = true;
        System.out.println("Memory cleaner: no HUD layer shows " + SHIP_VIEW_CLASS +
                " views, the client changed: ships are not released");
    }

    private boolean isHero(long address) {
        return address == main.hero.address || address == main.hero.pet.address;
    }

    private static final class TrackedShip {
        private final long trait, view, viewVtable, vtable, idOffset, containerOffset, traitsOffset;
        private final int id;
        private int removedChecks;

        private TrackedShip(long trait, long view, long viewVtable, long vtable, int id, long idOffset,
                            long containerOffset, long traitsOffset) {
            this.trait = trait;
            this.view = view;
            this.viewVtable = viewVtable;
            this.vtable = vtable;
            this.id = id;
            this.idOffset = idOffset;
            this.containerOffset = containerOffset;
            this.traitsOffset = traitsOffset;
        }
    }
}
