package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.other.Area;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager, StarSystemAPI {

    private final Main main;
    private final EventBrokerAPI eventBroker;
    private final StarManager starManager;

    public final EntityList entities;

    private long mapAddressStatic;
    private long viewAddressStatic;
    private long minimapAddressStatic;
    public long mapAddress;
    private long viewAddress;
    private long boundsAddress;
    public long eventAddress;

    public static int id = -1;
    public Lazy<Map> mapChange = new Lazy.NoCache<>();

    public ZoneInfo preferred;
    public ZoneInfo avoided;
    public Set<SafetyInfo> safeties;

    public static int internalWidth = 21000;
    public static int internalHeight = 13500;

    public static int clientWidth;
    public static int clientHeight;

    public double boundX;
    public double boundY;
    public double boundMaxX;
    public double boundMaxY;
    public double width;
    public double height;
    public final RectangleImpl screenBound = new RectangleImpl();
    private final RectangleImpl mapBound = new RectangleImpl();

    private final ObjArray minimapLayers = ObjArray.ofVector(true);
    private final Location pingLocationCache = new Location();
    public Location pingLocation = null;

    public MapManager(Main main,
                      PluginAPI pluginAPI,
                      EventBrokerAPI eventBroker,
                      StarManager starManager) {
        this.main = main;
        this.eventBroker = eventBroker;
        this.starManager = starManager;

        this.entities = pluginAPI.requireInstance(EntityList.class);
    }


    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> {
            eventAddress = value + 200;
            viewAddressStatic = value + 216;
            minimapAddressStatic = value + 224;
            mapAddressStatic = value + 256;
        });
        botInstaller.invalid.add(invalid -> {
            if (invalid) entities.clear();
        });
    }

    public void tick() {
        long temp = API.readMemoryLong(mapAddressStatic);

        if (mapAddress != temp) {
            update(temp);
        } else {
            pingLocation = getEnemyLocatorTarget();
            entities.update();
        }

        updateBounds();
        checkMirror();
    }

    private void update(long address) {
        mapAddress = address;

        internalWidth = API.readMemoryInt(address + 68);
        internalHeight = API.readMemoryInt(address + 72);
        if (internalHeight == 13100) internalHeight = 13500;
        if (internalHeight == 26200) internalHeight = 27000;

        mapBound.set(0, 0, internalWidth, internalHeight);

        int currMap = API.readMemoryInt(address + 76);
        boolean switched = currMap != id;
        if (switched) {
            id = currMap;

            Map old = main.hero.map;
            Map next = main.hero.map = main.starManager.byId(id);

            eventBroker.sendEvent(new MapChangeEvent(old, next));

            updateAreas(false);
        }
        entities.update(address);
        if (switched) mapChange.send(main.hero.map);
    }

    public void updateAreas(boolean createSafeties) {
        preferred = ConfigEntity.INSTANCE.getOrCreatePreferred();
        avoided = ConfigEntity.INSTANCE.getOrCreateAvoided();
        safeties = ConfigEntity.INSTANCE.getOrCreateSafeties();
        if (createSafeties) {
            for (Collection<? extends Entity> entities : entities.allEntities) {
                for (Entity e : entities) ConfigEntity.INSTANCE.updateSafetyFor(e);
            }
        }
    }

    private void checkMirror() {
        long temp = API.readMemoryLong(eventAddress) + 4 * 14;

        if (API.readMemoryBoolean(temp)) {
            API.writeMemoryInt(temp, 0);
        }
    }

    public ViewBounds viewBounds = new ViewBounds();

    // viewAddress
    // 2D
    //   Slot[168] int
    //   Slot[172] int
    //   Slot[176] Boolean
    //   Slot[180] Boolean
    //   Slot[184] starling.core::Starling
    //   Slot[192] _-73v::_-X5l
    //   Slot[200] _-e4L::HUD
    //   Slot[208] net.bigpoint.darkorbit.map.view2D::_-v25
    //   Slot[216] net.bigpoint.darkorbit.map.view2D::_-C4s
    //   Slot[224] net.bigpoint.darkorbit.map.model::_-332
    //   Slot[232] __AS3__.vec::Vector.<net.bigpoint.darkorbit.map.common::_-kb>
    //   Slot[240] __AS3__.vec::Vector.<String>
    //   Slot[248] _-J4u::_-4L
    //   Slot[256] flash.display::Bitmap
    //   Slot[264] flash.geom::Matrix
    //   Slot[272] Number


    // 3D
    //   Slot[168] int
    //   Slot[172] int
    //   Slot[176] Boolean
    //   Slot[180] Boolean
    //   Slot[184] int
    //   Slot[192] flash.geom::Vector3D
    //   Slot[200] starling.core::Starling
    //   Slot[208] _-e4L::HUD
    //   Slot[216] net.bigpoint.darkorbit.map.view3D::_-U4m
    //   Slot[224] net.bigpoint.darkorbit.map.model::_-332
    //   Slot[232] _-d5C::Stage3DManager
    //   Slot[240] _-d5C::_-T3i
    //   Slot[248] _-73v::_-X5l
    //   Slot[256] _-J4u::_-4L

    private boolean is3DView;
    private void updateBounds() {
        long temp = API.readMemoryLong(viewAddressStatic);

        if (viewAddress != temp) {
            viewAddress = temp;

            is3DView = !main.settingsManager.is2DForced()
                       && ByteUtils.readObjectName(API.readLong(viewAddress + 208)).contains("HUD");
            boundsAddress = API.readMemoryLong(viewAddress + (is3DView ? 216 : 208));
        }

        clientWidth = API.readMemoryInt(boundsAddress + 168);
        clientHeight = API.readMemoryInt(boundsAddress + 172);

        long updated = API.readMemoryLong(boundsAddress + (is3DView ? 320 : 280));
        updated = API.readMemoryLong(updated + 112);

        viewBounds.update(updated);

        boundX = API.readMemoryDouble(updated + 80);
        boundY = API.readMemoryDouble(updated + 88);
        boundMaxX = API.readMemoryDouble(updated + 112);
        boundMaxY = API.readMemoryDouble(updated + 120);
        screenBound.set(boundX, boundY, boundMaxX, boundMaxY);
        width = boundMaxX - boundX;
        height = boundMaxY - boundY;
    }

    public static class ViewBounds extends UpdatableAuto {
        public double leftTopX, leftTopY;
        public double rightTopX, rightTopY;
        public double rightBotX, rightBotY;
        public double leftBotX, leftBotY;

        @Override
        public void update() {
            this.leftTopX = API.readMemoryDouble(address + 80);
            this.leftTopY = API.readMemoryDouble(address + 88);
            this.rightTopX = API.readMemoryDouble(address + 96);
            this.rightTopY = API.readMemoryDouble(address + 104);
            this.rightBotX = API.readMemoryDouble(address + 112);
            this.rightBotY = API.readMemoryDouble(address + 120);
            this.leftBotX = API.readMemoryDouble(address + 128);
            this.leftBotY = API.readMemoryDouble(address + 136);
        }
    }

    private Location getEnemyLocatorTarget() {
        long temp = API.readMemoryLong(minimapAddressStatic); // Minimap
        double minimapX = API.readMemoryInt(temp + 0xA8);

        temp = API.readMemoryLong(temp + 0xF8); // LayeredSprite
        temp = API.readMemoryLong(temp + 0xA8); // Vector<Layer>
        minimapLayers.update(temp);

        for (int i = minimapLayers.getSize() - 1; i >= 0; i--) {
            long layer = minimapLayers.get(i); // Seems to be offset by 1 for some reason.
            long layerIdx = API.readMemoryInt(layer + 0xA8);

            if (layerIdx != Integer.MAX_VALUE) continue;

            double scale = (internalWidth / minimapX) / 20;
            long sprites = API.readMemoryLong(layer, 0x48);
            if (findMarker(sprites, scale, pingLocationCache)) return pingLocationCache;
        }
        return null;
    }

    private boolean findMarker(long spriteArray, double scale, Location result) {
        int size = API.readMemoryInt(spriteArray, 0x40, 0x18);
        // Always try to iterate at least once.
        // With 0 or 1 elements, it seems to be implemented as a singleton and size isn't updated.
        // With 2 or more elements, it's a linked list of elements to follow at 0x18.
        if (size == 0)
            return isMarker(API.readMemoryLong(spriteArray, 0x20), scale, result);

        long marker = API.readMemoryLong(spriteArray, 0x20);
        for (int i = 0; i < size; i++, marker = API.readMemoryLong(marker, 0x18)) {
            if (isMarker(marker, scale, result)) return true;
        }

        return false;
    }

    private boolean isMarker(long sprite, double scale, Location result) {
        if (sprite == 0) return false;

        int x = API.readMemoryInt(sprite + 0x58);
        int y = API.readMemoryInt(sprite + 0x5C);
        result.set(scale * x, scale * y);

        int halfWidth = internalWidth / 2, halfHeight = internalHeight / 2;
        // Ignore if 0,0, or further away from the center of the map than corner in manhattan distance
        if ((x == 0 && y == 0) || result.distance(halfWidth, halfHeight) > halfWidth + halfHeight) return false;

        String name = API.readMemoryString(API.readMemoryLong(sprite, 440, 0x10, 0x28, 0x90));
        if (name != null && name.equals("minimapmarker")) return true;

        String pointer = API.readMemoryString(API.readMemoryLong(sprite, 216, 0x10, 0x28, 0x90));
        return pointer == null || !pointer.equals("minimapPointer");
    }

    public boolean isTarget(Entity entity) {
        return API.readMemoryLong(API.readMemoryLong(mapAddress + 120) + 40) == entity.address;
    }

    public boolean isOutOfMap(double x, double y) {
        return x < 0 || y < 0 || x > internalWidth || y > internalHeight;
    }

    public boolean isCurrentTargetOwned() {
        if (is3DView)
            return Optional.ofNullable(main.hero.getTargetAs(Lockable.class))
                    .map(Lockable::isOwned)
                    .orElse(false);

        long temp = API.readMemoryLong(viewAddressStatic);
        temp = API.readMemoryLong(temp + 216); //
        temp = API.readMemoryLong(temp + 200); //
        temp = API.readMemoryLong(temp + 48); // get _target
        int lockStatus = API.readMemoryInt(temp + 40); // IntHolder.value
        // 1 = selected & owned
        // 2 = selected & someone else owns it
        // 3 = ?
        // 4 = ?
        return lockStatus == 1 || lockStatus < 1 || lockStatus > 4;
    }


    @Override
    public GameMap getCurrentMap() {
        return main.hero.map;
    }

    @Override
    public Area.Rectangle getCurrentMapBounds() {
        return mapBound;
    }

    @Override
    public Collection<? extends GameMap> getMaps() {
        return starManager.getMaps();
    }

    @Override
    public GameMap getById(int mapId) throws MapNotFoundException {
        return starManager.getById(mapId);
    }

    @Override
    public GameMap getOrCreateMapById(int mapId) {
        return starManager.byId(mapId);
    }

    @Override
    public GameMap getByName(@NotNull String mapName) throws MapNotFoundException {
        return starManager.getByName(mapName);
    }

    @Override
    public Portal findNext(GameMap targetMap) {
        return starManager.next(main.hero, starManager.byId(targetMap.getId()));
    }
}
