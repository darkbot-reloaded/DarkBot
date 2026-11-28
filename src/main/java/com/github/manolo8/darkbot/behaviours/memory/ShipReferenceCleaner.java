package com.github.manolo8.darkbot.behaviours.memory;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import eu.darkbot.util.Timer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import static com.github.manolo8.darkbot.Main.API;

/**
 * Releases ships that left the map.
 * <p>
 * The client keeps every removed ship until the next map change: the ship's HUD view, its info and top displays
 * keep pointing to the ship, and the ship's traits point back to it as their owner. Each of these references alone
 * keeps the ship reachable.
 * <p>
 * Ships are followed through the HUD layer of ship views, the one holding the hero's view. Its trait list holds the
 * info trait of every ship the HUD shows. A followed ship counts as removed when the HUD and the ship itself agree:
 * its trait left that list, and its container has been null for {@link #REMOVED_CHECKS} checks in a row. Then those
 * references are set to null, each only if it still points to the ship.
 * <p>
 * Views whose ship already left the HUD's list but still holds it are followed too: ships removed while the cleaner
 * was off, and the previous hero and pet, replaced after a death.
 */
class ShipReferenceCleaner implements LeakCleaner {

    /** Screen manager -> map view -> HUD -> its display layers (Vector). */
    private static final int SCREEN_MANAGER_MAP_VIEW = 0xD8, MAP_VIEW_HUD = 0xD8, HUD_LAYERS = 0xB8;
    /** HUD display layer: its views by trait (Dictionary), its HUD, the class of its views, its trait container. */
    private static final int LAYER_VIEWS = 0x20, LAYER_HUD = 0x28, LAYER_VIEW_CLASS = 0x38, LAYER_TRAITS = 0x40;
    /** Trait container: its traits (Vector). */
    private static final int TRAIT_CONTAINER_TRAITS = 0x30;
    /** Ship HUD view: its ship, info display and top display. */
    private static final int VIEW_SHIP = 0xD8, VIEW_INFO = 0xE8, VIEW_TOP = 0xF0;
    /** Info display: its ship and the ship's info trait. Top display: its ship. */
    private static final int INFO_SHIP = 0xE8, INFO_TRAIT = 0xF0, TOP_SHIP = 0xB0;
    /** Map entity: its traits (Vector), id and container. Entity trait: its owner. */
    private static final int ENTITY_TRAITS = 0x30, ENTITY_ID = 0x38, ENTITY_CONTAINER = 0x60, TRAIT_OWNER = 0x20;

    private static final long INTERVAL_MS = 1_000;
    /** The views are read when a ship appears, and at least this often for those whose ship already left. */
    private static final long VIEWS_CHECK_INTERVAL_MS = 60_000;
    private static final int REMOVED_CHECKS = 2;
    /** Checks in a row without the ship layer while the hero is on a map, before it is reported as not found. */
    private static final int LAYER_MISSES_REPORTED = 300;

    private final Main main;
    private final BotInstaller botInstaller;

    private final FlashListLong layers = FlashListLong.ofVector();
    private final FlashListLong views = FlashListLong.ofMapValues();
    private final FlashListLong layerTraits = FlashListLong.ofVector();
    private final FlashListLong shipTraits = FlashListLong.ofVector();
    private final Timer viewsCheck = Timer.get(VIEWS_CHECK_INTERVAL_MS);
    private final LongSet liveTraits = new LongOpenHashSet();
    /**
     * Live traits already dealt with: those of followed ships and of the hero's and pet's views. The views are read
     * when a live trait is not among them, i.e. when a ship appeared.
     */
    private final LongSet knownTraits = new LongOpenHashSet();
    private final Long2ObjectMap<TrackedShip> tracked = new Long2ObjectOpenHashMap<>();

    private long hud, shipLayer, shipViewClass;
    private int mapId = Integer.MIN_VALUE;
    private int layerMisses;
    private long released;
    private boolean layerMissReported;

    ShipReferenceCleaner(Main main, BotInstaller botInstaller) {
        this.main = main;
        this.botInstaller = botInstaller;
    }

    @Override
    public long getIntervalMs() {
        return INTERVAL_MS;
    }

    @Override
    public long getReleased() {
        return released;
    }

    @Override
    public String getReleasedName() {
        return "ships";
    }

    /** Follows the ships the HUD shows and releases the removed ones. Does nothing while the HUD can't be read. */
    @Override
    public void clean() {
        if (!followShipLayer() || !readLiveTraits()) return;
        knownTraits.retainAll(liveTraits);
        if (hasUnknownTrait() || viewsCheck.tryActivate()) trackShips();
        releaseRemovedShips();
    }

    @Override
    public void reset() {
        forgetShips();
        liveTraits.clear();
        hud = shipLayer = shipViewClass = 0;
        mapId = Integer.MIN_VALUE;
        layerMisses = 0;
        viewsCheck.disarm();
    }

    /**
     * Finds the ship layer of the current HUD, or checks that the one found before still belongs to it.
     * A new map or HUD means a new set of ships: followed ships are forgotten.
     */
    private boolean followShipLayer() {
        int currentMap = main.hero.map != null ? main.hero.map.id : Integer.MIN_VALUE;
        if (currentMap != mapId) {
            forgetShips();
            mapId = currentMap;
        }

        long currentHud = findHud();
        if (currentHud == 0) return false;
        if (currentHud != hud) {
            forgetShips();
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

    private void forgetShips() {
        tracked.clear();
        knownTraits.clear();
    }

    private long findHud() {
        Long screenManager = botInstaller.screenManagerAddress.get();
        if (screenManager == null || screenManager == 0) return 0;
        return API.readLong(screenManager, SCREEN_MANAGER_MAP_VIEW, MAP_VIEW_HUD);
    }

    /** Whether the layer is still the ship layer found in the current HUD. */
    private boolean isShipLayer(long layer) {
        return layer != 0 && API.readLong(layer + LAYER_HUD) == hud
                && API.readLong(layer + LAYER_VIEW_CLASS) == shipViewClass;
    }

    /** The layer of the current HUD that holds the hero's view, so the ship views: 0 if none (yet). */
    private long findShipLayer() {
        long hero = main.hero.address;
        if (hero == 0) return 0;
        layers.update(API.readLong(hud + HUD_LAYERS));
        for (int i = 0; i < layers.size(); i++) {
            long layer = layers.getLong(i);
            if (API.readLong(layer + LAYER_HUD) != hud) continue;
            views.update(API.readLong(layer + LAYER_VIEWS));
            for (int v = 0; v < views.size(); v++) {
                if (API.readLong(views.getLong(v) + VIEW_SHIP) != hero) continue;
                shipViewClass = API.readLong(layer + LAYER_VIEW_CLASS);
                return layer;
            }
        }
        return 0;
    }

    /** Reads the traits of the ships the HUD shows. False if they can't be read: then no ship is released. */
    private boolean readLiveTraits() {
        liveTraits.clear();
        layerTraits.update(API.readLong(shipLayer, LAYER_TRAITS, TRAIT_CONTAINER_TRAITS));
        if (!layerTraits.isReadable()) return false;
        for (int i = 0; i < layerTraits.size(); i++) {
            long trait = layerTraits.getLong(i);
            if (trait != 0) liveTraits.add(trait);
        }
        return true;
    }

    private boolean hasUnknownTrait() {
        for (LongIterator it = liveTraits.iterator(); it.hasNext(); )
            if (!knownTraits.contains(it.nextLong())) return true;
        return false;
    }

    /**
     * Starts following the ships of the views not followed yet: those the HUD shows, and those whose ship left it but
     * is still held. A ship that can't be followed now is tried again the next time the views are read.
     */
    private void trackShips() {
        views.update(API.readLong(shipLayer + LAYER_VIEWS));
        if (!views.isReadable()) return;
        for (int i = 0; i < views.size(); i++) {
            long view = views.getLong(i);
            long trait = API.readLong(view, VIEW_INFO, INFO_TRAIT);
            if (knownTraits.contains(trait)) continue;
            long ship = API.readLong(view + VIEW_SHIP);
            if (ship == 0 || tracked.containsKey(ship)) continue;
            boolean listed = liveTraits.contains(trait);
            if (isHero(ship)) {
                if (listed) knownTraits.add(trait);
                continue;
            }
            TrackedShip t = track(ship, trait, view, listed);
            if (t == null) continue;
            tracked.put(ship, t);
            if (listed) knownTraits.add(trait);
        }
    }

    /**
     * A ship must own the trait of its view, and be on the map while the HUD lists it or off it once it doesn't.
     * Otherwise an offset no longer matches the client, which could make live ships look removed: such ships are
     * never followed.
     */
    private TrackedShip track(long ship, long trait, long view, boolean listed) {
        boolean onMap = API.readLong(ship + ENTITY_CONTAINER) != 0;
        if (API.readLong(trait + TRAIT_OWNER) != ship || onMap != listed) return null;
        return new TrackedShip(trait, view, ByteUtils.readVtable(view), ByteUtils.readVtable(ship),
                API.readInt(ship + ENTITY_ID));
    }

    /** Releases the followed ships the HUD no longer lists and that stayed off the map. */
    private void releaseRemovedShips() {
        ObjectIterator<Long2ObjectMap.Entry<TrackedShip>> it = tracked.long2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            Long2ObjectMap.Entry<TrackedShip> entry = it.next();
            TrackedShip t = entry.getValue();
            // Still listed: on the map, nothing to read
            if (liveTraits.contains(t.trait)) {
                t.removedChecks = 0;
                continue;
            }
            long ship = entry.getLongKey();
            // Memory reused by another object
            if (ByteUtils.readVtable(ship) != t.vtable || API.readInt(ship + ENTITY_ID) != t.id) {
                it.remove();
                continue;
            }
            if (API.readLong(ship + ENTITY_CONTAINER) != 0) {
                t.removedChecks = 0;
                continue;
            }
            if (++t.removedChecks < REMOVED_CHECKS) continue;

            release(ship, t);
            it.remove();
            released++;
        }
    }

    private void release(long ship, TrackedShip t) {
        // The view is only touched while it is still the same object that was followed
        if (ByteUtils.readVtable(t.view) == t.viewVtable && API.readLong(t.view + VIEW_SHIP) == ship) {
            long info = API.readLong(t.view + VIEW_INFO);
            if (info != 0 && API.readLong(info + INFO_SHIP) == ship) {
                long trait = API.readLong(info + INFO_TRAIT);
                if (trait != 0 && API.readLong(trait + TRAIT_OWNER) == ship) clear(info, INFO_TRAIT, trait);
                clear(info, INFO_SHIP, ship);
            }
            clear(API.readLong(t.view + VIEW_TOP), TOP_SHIP, ship);
            clear(t.view, VIEW_SHIP, ship);
        }

        shipTraits.update(API.readLong(ship + ENTITY_TRAITS));
        for (int i = 0; i < shipTraits.size(); i++) clear(shipTraits.getLong(i), TRAIT_OWNER, ship);
    }

    /** Sets the field of the object to null if it still holds the expected object. */
    private static void clear(long object, int field, long expected) {
        if (object != 0) API.replaceLong(object + field, expected, 0);
    }

    /**
     * The hero always has a view in the ship layer, so a layer that stays missing while the hero is on a map means
     * the offsets no longer match the client. Reported once.
     */
    private void reportMissingShipLayer() {
        if (main.hero.address == 0 || main.hero.map == null) return;
        if (layerMissReported || ++layerMisses < LAYER_MISSES_REPORTED) return;
        layerMissReported = true;
        System.out.println("Memory cleaner: the HUD layer of ship views was not found, the offsets may be outdated: " +
                "ships are not released");
    }

    private boolean isHero(long address) {
        return address == main.hero.address || address == main.hero.pet.address;
    }

    private static final class TrackedShip {
        private final long trait, view, viewVtable, vtable;
        private final int id;
        private int removedChecks;

        private TrackedShip(long trait, long view, long viewVtable, long vtable, int id) {
            this.trait = trait;
            this.view = view;
            this.viewVtable = viewVtable;
            this.vtable = vtable;
            this.id = id;
        }
    }
}
