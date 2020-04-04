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

import java.util.Set;

import static com.github.manolo8.darkbot.Main.API;

public class MapManager implements Manager {

    private final Main main;

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

    private ObjArray minimapLayers = ObjArray.ofVector(true);
    public Location pingLocation = null;

    public MapManager(Main main) {
        this.main = main;

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

    }

    public void tick() {
        long temp = API.readMemoryLong(mapAddressStatic);

        if (mapAddress != temp) {
            update(temp);
        } else {
            pingLocation = updateMinimap();
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
        int currMap = API.readMemoryInt(address + 76);
        boolean switched = currMap != id;
        if (switched) {
            id = currMap;
            main.hero.map = main.starManager.byId(id);
            preferred = ConfigEntity.INSTANCE.getOrCreatePreferred();
            avoided = ConfigEntity.INSTANCE.getOrCreateAvoided();
            safeties = ConfigEntity.INSTANCE.getOrCreateSafeties();
        }
        entities.update(address);
        if (switched) mapChange.send(main.hero.map);
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
        width = boundMaxX - boundX;
        height = boundMaxY - boundY;
    }

    private Location updateMinimap() {
        long temp = API.readMemoryLong(minimapAddressStatic); // Minimap
        double minimapX = API.readMemoryInt(temp + 0xA8);

        temp = API.readMemoryLong(temp + 0xF8); // LayeredSprite
        temp = API.readMemoryLong(temp + 0xA8); // Vector<Layer>
        minimapLayers.update(temp);

        for (int i = 0; i < minimapLayers.size; i++) {
            long layer = minimapLayers.get(i); // Seems to be offset by 1 for some reason.
            long layerIdx = API.readMemoryInt(layer + 0xA8);

            if (layerIdx != Integer.MAX_VALUE) continue;

            int x = API.readMemoryInt(API.readMemoryLong(layer, 0x48, 0x20, 0x18) + 0x58);
            int y = API.readMemoryInt(API.readMemoryLong(layer, 0x48, 0x20, 0x18) + 0x5C);

            double scale = (internalWidth / minimapX) / 20;

            return new Location(scale * x, scale * y);
        }

        return null;
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

}
