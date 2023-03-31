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
import com.github.manolo8.darkbot.core.entities.Player;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.entities.Relay;
import com.github.manolo8.darkbot.core.entities.SpaceBall;
import com.github.manolo8.darkbot.core.entities.StaticEntity;
import com.github.manolo8.darkbot.core.entities.Unknown;
import com.github.manolo8.darkbot.core.entities.bases.BaseHangar;
import com.github.manolo8.darkbot.core.entities.bases.BaseHeadquarters;
import com.github.manolo8.darkbot.core.entities.bases.BaseRefinery;
import com.github.manolo8.darkbot.core.entities.bases.BaseRepairStation;
import com.github.manolo8.darkbot.core.entities.bases.BaseStation;
import com.github.manolo8.darkbot.core.entities.bases.BaseTurret;
import com.github.manolo8.darkbot.core.entities.bases.QuestGiver;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.utils.Offsets;
import org.intellij.lang.annotations.Language;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.manolo8.darkbot.Main.API;

public enum EntityFactory implements EntityBuilder {
    BOX             (Box::new,           "box_.*"),
    ORE             (Box.Ore::new,       "ore_.*"),
    X2_BEACON       (Box.Beacon::new,    "beacon_.*"), //todo use me
    MINE            (Mine::new,          "mine_.*"),
    FIREWORK        (Entity::new,        "firework_box"),

    LOW_RELAY       (Relay::new,         "relay"),
    NPC_BEACON      (MapNpc::new,        "npc-beacon.*"), //todo use me
    SPACE_BALL      (SpaceBall::new,     "mapIcon_spaceball"),

    CBS_ASTEROID    (BattleStation.Asteroid::new, "asteroid"),
    CBS_CONSTRUCTION(BattleStation::new, "cbs-construction"),
    CBS_MODULE      (BattleStation.Module::new, "wreck|module_.*"), // addr+112 moduleType string
    CBS_MODULE_CON  (BattleStation::new, "module-construction"),
    CBS_STATION     (BattleStation.Built::new, "battleStation"),

    // Aegis/hammerclaw healing pods
    POD_HEAL              (StaticEntity.PodHeal::new,              "pod_heal"),
    BUFF_CAPSULE          (StaticEntity.BuffCapsule::new,          "buffCapsule_.*"),
    BURNING_TRAIL         (StaticEntity.BurningTrail::new,         "burning_trail_entity_1"),
    BURNING_TRAIL_ENEMY   (StaticEntity.BurningTrailEnemy::new,    "burning_trail_entity_2"),
    PLUTUS_GENERATOR      (StaticEntity.PlutusGenerator::new,      "plutus-generator"),
    // red & green are different class types in-game
    PLUTUS_GENERATOR_RED  (StaticEntity.PlutusGenerator::new,      "plutus-generator-red"),
    PLUTUS_GENERATOR_GREEN(StaticEntity.PlutusGeneratorGreen::new, "plutus-generator-green"),
    PET_BEACON            (StaticEntity.PetBeacon::new,            "pet_beacon"),

    BASE_TURRET     (BaseTurret::new,       "turret_.*"),       // Turrets around x-1 and x-8 bases
    REFINERY        (BaseRefinery::new,     "refinery_.*"),     // Refinery to sell ores at
    BASE_HANGAR     (BaseHangar::new,       "hangar_.*"),       // Hangar inside bases
    HEADQUARTER     (BaseHeadquarters::new, "headquarters_.*"), // Headquarters in middle of x-1 and x-8
    REPAIR_STATION  (BaseRepairStation::new,"repairstation_.*"),// Repair station inside bases
    QUEST_GIVER     (QuestGiver::new,       "questgiver_.*"),   // Quest givers on x-1, x-4, x-5 and x-8
    BASE_STATION    (BaseStation::new,      "station_.*"),      // Standalone station on 5-2
    CTB_HOME_ZONE   (BasePoint::new,        "ctbHomeZone_.*"),  //todo use me

    PORTAL   (EntityFactory::getOrCreatePortal, "\\d+$"),

    ZONE     (EntityFactory::isZone, EntityFactory::defineZoneType), // Generic zone, redirects to BARRIER or MIST_ZONE
    BARRIER  (Barrier::new),
    MIST_ZONE(NoCloack::new),

    PET      (Pet::new, EntityFactory::isPet),
    SHIP     (EntityFactory::isShip, EntityFactory::defineShipType), // Generic ship, redirects to PLAYER or NPC
    PLAYER   (Player::new),
    NPC      (Npc::new);

    // Constructor to create the entity of this type
    private final BiFunction<Integer, Long, ? extends Entity> constructor;
    // Test if the type matches for the asset & address provided
    private final BiPredicate<String, Long> typeMatcher;
    // Will mutate the type to a different (specialized) type in the bot
    private final Function<Long, EntityBuilder> typeModifier;


    EntityFactory(BiFunction<Integer, Long, Entity> constructor) {
        this(constructor, (asset, addr) -> false, null);
    }

    EntityFactory(BiFunction<Integer, Long, Entity> constructor, @Language("RegExp") String regex) {
        this(constructor, regexMatcher(regex));
    }

    EntityFactory(BiFunction<Integer, Long, Entity> constructor, BiPredicate<String, Long> typeMatcher) {
        this(constructor, typeMatcher, null);
    }

    EntityFactory(BiPredicate<String, Long> typeMatcher, Function<Long, EntityBuilder> typeModifier) {
        this(null, typeMatcher, typeModifier);
    }

    EntityFactory(BiFunction<Integer, Long, Entity> constructor, BiPredicate<String, Long> typeMatcher, Function<Long, EntityBuilder> typeModifier) {
        this.constructor  = constructor;
        this.typeModifier = typeModifier;
        this.typeMatcher  = typeMatcher;
    }

    private static BiPredicate<String, Long> regexMatcher(String regex) {
        Pattern p = Pattern.compile(regex);
        return (asset, addr) -> p.matcher(asset).matches();
    }

    public EntityBuilder get(long address) {
        return this.typeModifier == null ? this : this.typeModifier.apply(address);
    }

    public Entity createEntity(int id, long address) {
        return this.constructor == null ? null : this.constructor.apply(id, address);
    }

    public static EntityBuilder find(long address) {
        String assetId = Offsets.getEntityAssetId(address);

        for (EntityFactory type : EntityFactory.values()) {
            if (type.typeMatcher.test(assetId, address)) return type;
        }

        return createUnknown(assetId);
    }

    private static String getZoneKey(long address) {
        return API.readMemoryString(address, 136).trim();
    }

    private static boolean isZone(String asset, long address) {
        String key = getZoneKey(address);
        return key.equals("NOA") || key.equals("DMG") || key.equals("TRG");
    }

    private static boolean isPet(String asset, long address) {
        return API.readMemoryString(address, 192, 144).trim().equals("pet");
    }

    private static boolean isShip(String asset, long address) {
        int id      = API.readMemoryInt(address + 56);
        int isNpc   = API.readMemoryInt(address + 112);
        int visible = API.readMemoryInt(address + 116);
        int c       = API.readMemoryInt(address + 120);
        int d       = API.readMemoryInt(address + 124);

        return id > 0 && (isNpc == 1 || isNpc == 0) &&
                (visible == 1 || visible == 0) && (c == 1 || c == 0) && d == 0;
    }

    private static EntityBuilder defineZoneType(long address) {
        String key = getZoneKey(address);
        return key.equals("NOA") ? BARRIER : key.equals("DMG") ? MIST_ZONE : createUnknown(key);
    }

    private static EntityBuilder defineShipType(long address) {
        int isNpc = API.readMemoryInt(address + 112);
        return isNpc == 1 ? NPC : isNpc == 0 ? PLAYER : createUnknown(null);
    }

    private static Portal getOrCreatePortal(int id, long address) {
        int portalType = API.readMemoryInt(address + Portal.TYPE_OFFSET);
        int x          = (int) API.readMemoryDouble(address, 64, 32);
        int y          = (int) API.readMemoryDouble(address, 64, 40);

        Portal portal = StarManager.getInstance().getOrCreate(id, portalType, x, y);
        portal.update(address);

        return portal;
    }

    private static EntityBuilder createUnknown(String assetId) {
        return (id, addr) -> new Unknown(id, addr, assetId);
    }
}