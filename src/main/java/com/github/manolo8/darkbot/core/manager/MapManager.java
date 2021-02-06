package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.config.ZoneInfo;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.EntityList;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.core.utils.pathfinder.RectangleImpl;
import com.github.manolo8.darkbot.utils.debug.ReadObjNames;
import eu.darkbot.api.entities.Portal;
import eu.darkbot.api.entities.utils.Area;
import eu.darkbot.api.managers.EventSenderAPI;
import eu.darkbot.api.managers.StarSystemAPI;

import java.util.Collection;
import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager, StarSystemAPI {

    private final Main main;
    private final EventSenderAPI eventSender;
    private final StarManager starManager;

    public final EntityList entities;

    private long mapAddressStatic;
    private long viewAddressStatic;
    private long minimapAddressStatic;
    private long mapAddress;
    private long viewAddress;
    private long boundsAddress;
    long eventAddress;

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
    private final RectangleImpl bound = new RectangleImpl();

    private final ObjArray minimapLayers = ObjArray.ofVector(true);
    private final Location pingLocationCache = new Location();
    public Location pingLocation = null;

    public MapManager(Main main,
                      EventSenderAPI eventSender,
                      StarManager starManager) {
        this.main = main;
        this.eventSender = eventSender;
        this.starManager = starManager;

        this.entities = new EntityList(main);
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
        int currMap = API.readMemoryInt(address + 76);
        boolean switched = currMap != id;
        if (switched) {
            id = currMap;

            Map old = main.hero.map;
            Map next = main.hero.map = main.starManager.byId(id);

            eventSender.sendEvent(new MapChangeEvent(old, next));

            updateAreas();
        }
        entities.update(address);
        if (switched) mapChange.send(main.hero.map);
    }

    public void updateAreas() {
        preferred = ConfigEntity.INSTANCE.getOrCreatePreferred();
        avoided = ConfigEntity.INSTANCE.getOrCreateAvoided();
        safeties = ConfigEntity.INSTANCE.getOrCreateSafeties();
    }

    private void checkMirror() {
        long temp = API.readMemoryLong(eventAddress) + 4 * 14;

        if (API.readMemoryBoolean(temp)) {
            API.writeMemoryInt(temp, 0);
        }
    }

    private void updateBounds() {
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
        bound.set(boundX, boundY, boundMaxX, boundMaxY);
        width = boundMaxX - boundX;
        height = boundMaxY - boundY;
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

            long sprites = API.readMemoryLong(layer, 0x48);
            long sprite = findMarker(sprites);

            if (sprite == -1) return null;

            int x = API.readMemoryInt(sprite + 0x58);
            int y = API.readMemoryInt(sprite + 0x5C);

            double scale = (internalWidth / minimapX) / 20;
            return pingLocationCache.set(scale * x, scale * y);
        }
        return null;
    }

    private long findMarker(long spriteArray) {
        int size = API.readMemoryInt(spriteArray, 0x40, 0x18);
        // Always try to iterate at least once.
        // With 0 or 1 elements, it seems to be implemented as a singleton and size isn't updated.
        // With 2 or more elements, it's a linked list of elements to follow at 0x18.
        if (size == 0) size = 1;

        long currSprite = spriteArray;
        for (int spriteIdx = 0; spriteIdx < size; spriteIdx++) {
            currSprite = API.readMemoryLong(currSprite, spriteIdx == 0 ? 0x20 : 0x18);
            if (currSprite == 0) return -1; // Invalid to continue

            String name = API.readMemoryString(API.readMemoryLong(currSprite, 440, 0x10, 0x28, 0x90));
            if (name != null && name.equals("minimapmarker")) return currSprite;
        }
        return -1;
    }

    public boolean isTarget(Entity entity) {
        return API.readMemoryLong(API.readMemoryLong(mapAddress + 120) + 40) == entity.address;
    }

    public boolean isOutOfMap(double x, double y) {
        return x < 0 || y < 0 || x > internalWidth || y > internalHeight;
    }

    public boolean isCurrentTargetOwned() {
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
    public eu.darkbot.api.entities.utils.Map getCurrentMap() {
        return main.hero.map;
    }

    @Override
    public Area.Rectangle getCurrentMapBounds() {
        return bound;
    }

    @Override
    public Collection<? extends eu.darkbot.api.entities.utils.Map> getMaps() {
        return starManager.getMaps();
    }

    @Override
    public eu.darkbot.api.entities.utils.Map getById(int mapId) throws MapNotFoundException {
        return starManager.getById(mapId);
    }

    @Override
    public eu.darkbot.api.entities.utils.Map getOrCreateMapById(int mapId) {
        return starManager.byId(mapId);
    }

    @Override
    public eu.darkbot.api.entities.utils.Map getByName(String mapName) throws MapNotFoundException {
        return starManager.getByName(mapName);
    }

    @Override
    public Portal findNext(eu.darkbot.api.entities.utils.Map targetMap) {
        return starManager.next(main.hero, starManager.byId(targetMap.getId()));
    }
}
