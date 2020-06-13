package com.github.manolo8.darkbot.core.utils.factory;

import com.github.manolo8.darkbot.core.entities.Barrier;
import com.github.manolo8.darkbot.core.entities.BasePoint;
import com.github.manolo8.darkbot.core.entities.BattleStation;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.MapNpc;
import com.github.manolo8.darkbot.core.entities.Mine;
import com.github.manolo8.darkbot.core.entities.NoCloack;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Pet;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Ship;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import org.intellij.lang.annotations.Language;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.manolo8.darkbot.Main.API;

public enum EntityFactory {
    BOX             (Box::new,           "box_.*"),
    ORE             (Box::new,           "ore_.*"),
    MINE            (Mine::new,          "mine_.*"),
    FIREWORK        (Entity::new,        "firework_box"),
    X2_BEACON       (BasePoint::new,     "beacon_.*"),

    LOW_RELAY       (MapNpc::new,        "relay"),
    NPC_BEACON      (MapNpc::new,        "npc-beacon.*"),
    SPACE_BALL      (MapNpc::new,        "mapIcon_spaceball"),

    CBS_ASTEROID    (BattleStation::new, "asteroid"),
    CBS_CONSTRUCTION(BattleStation::new, "cbs-construction"),
    CBS_MODULE      (BattleStation::new, "wreck|module_.*"), // addr+112 moduleType string
    CBS_MODULE_CON  (BattleStation::new, "module-construction"),
    CBS_STATION     (BattleStation::new, "battleStation"),

    POD_HEAL        (BasePoint::new,     "pod_heal"),
    BUFF_CAPSULE    (BasePoint::new,     "buffCapsule_.*"),
    BURNING_TRAIL   (BasePoint::new,     "burning_trail_entity_.*"),
    PLUTUS_GENERATOR(BasePoint::new,     "plutus-generator"),

    REFINERY        (BasePoint::new,     "refinery_.*"),
    HOME_ZONE       (BasePoint::new,     "ctbHomeZone_.*"),
    BASE_TURRET     (BasePoint::new,     "turret_.*"),
    BASE_HANGAR     (BasePoint::new,     "hangar_.*"),
    BASE_STATION    (BasePoint::new,     "station_.*"),
    HEADQUARTER     (BasePoint::new,     "headquarters_.*"),
    QUEST_GIVER     (BasePoint::new,     "questgiver_.*"),
    REPAIR_STATION  (BasePoint::new,     "repairstation_.*"),

    PORTAL   (EntityFactory::getOrCreatePortal, "[0-9]+$"),

    BARRIER  (Barrier::new, EntityFactory::defineZoneType, "NOA|DMG"),
    MIST_ZONE(NoCloack::new),

    SHIP     (Ship::new, EntityFactory::defineShipType),
    NPC      (Npc::new),

    PET      (Pet::new),
    UNKNOWN  (Entity::new),
    NONE();

    private final Pattern pattern;
    private final Function<Long, EntityFactory> customType;
    private final BiFunction<Integer, Long, ? extends Entity> constructor;

    EntityFactory() { this(null, null, null); }
    EntityFactory(BiFunction<Integer, Long, Entity> constructor) { this(constructor, null, null); }
    EntityFactory(BiFunction<Integer, Long, Entity> constructor, @Language("RegExp") String regex) { this(constructor, null, regex); }
    EntityFactory(BiFunction<Integer, Long, Entity> constructor, Function<Long, EntityFactory> customType) { this(constructor, customType, null); }
    EntityFactory(BiFunction<Integer, Long, Entity> constructor, Function<Long, EntityFactory> customType, @Language("RegExp") String regex) {
        this.constructor = constructor;
        this.customType  = customType;
        this.pattern     = regex == null ? null : Pattern.compile(regex);
    }

    public EntityFactory get(long address) {
        return this.customType == null ? this : this.customType.apply(address);
    }

    public Entity createEntity(int id, long address) {
        return this.constructor == null ? new Entity(id, address) : this.constructor.apply(id, address);
    }

    public static EntityFactory find(int id, long address) {
        String assetId = getAssetId(address);

        for (EntityFactory type : EntityFactory.values()) {
            if (type.pattern == null) continue;
            if (type.pattern.matcher(type == BARRIER ? getZoneKey(address) :
                                             assetId).matches()) return type;
        }

        return isPet(address) ? PET : isShip(address, id) ? SHIP : UNKNOWN;
    }

    private static EntityFactory defineZoneType(long address) {
        String key = getZoneKey(address);
        return key.equals("NOA") ? BARRIER : key.equals("DMG") ? MIST_ZONE : UNKNOWN;
    }

    private static EntityFactory defineShipType(long address) {
        int isNpc = API.readMemoryInt(address + 112);
        return isNpc == 1 ? NPC : isNpc == 0 ? SHIP : UNKNOWN;
    }

    private static String getZoneKey(long address) {
        return API.readMemoryString(address, 136).trim();
    }

    private static String getAssetId(long address) {
        long temp = API.readMemoryLong(address, 48, 48, 16) & ByteUtils.FIX;
        return API.readMemoryString(temp, 64, 32, 24, 8, 16, 24).trim();
    }

    private static boolean isPet(long address) {
        return API.readMemoryString(address, 192, 136).trim().equals("pet");
    }

    private static boolean isShip(long address, int id) {
        int isNpc   = API.readMemoryInt(address + 112);
        int visible = API.readMemoryInt(address + 116);
        int c       = API.readMemoryInt(address + 120);
        int d       = API.readMemoryInt(address + 124);

        return id > 0 && (isNpc == 1 || isNpc == 0) &&
                (visible == 1 || visible == 0) && (c == 1 || c == 0) && d == 0;
    }

    private static Portal getOrCreatePortal(int id, long address) {
        int portalType = API.readMemoryInt(address + 112);
        int x          = (int) API.readMemoryDouble(address, 64, 32);
        int y          = (int) API.readMemoryDouble(address, 64, 40);

        Portal portal = StarManager.getInstance().getOrCreate(id, portalType, x, y);
        portal.update(address);

        return portal;
    }
}