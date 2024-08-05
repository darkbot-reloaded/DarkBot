package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import eu.darkbot.api.API;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;

import static com.github.manolo8.darkbot.Main.API;

public class EffectManager implements Manager, API.Singleton {
    private long mapAddressStatic;

    private final FlashListLong effectsPtr = FlashListLong.ofVector();
    private final Long2ObjectMap<EffectIntSet> effects = new Long2ObjectOpenHashMap<>();

    public EffectManager(Main main) {
    }

    @Override
    public void install(BotInstaller botInstaller) {
        effects.clear();
        botInstaller.screenManagerAddress.add(value -> mapAddressStatic = value + 256);
    }

    public void tick() {
        effectsPtr.update(API.readLong(API.readLong(mapAddressStatic), 136, 48));
        effects.values().removeIf(EffectIntSet::clearOrExpire);

        for (int i = 0; i < effectsPtr.size(); i++) {
            long addr = effectsPtr.getLong(i);
            int id = API.readInt(addr + 0x24);
            long entity = API.readLong(addr + 0x48);

            effects.computeIfAbsent(entity, key -> new EffectIntSet()).add(id);
        }
    }

    public IntSet getEffects(Entity entity) {
        if (entity.address == 0) return IntSet.of();
        IntSet val = effects.get(entity.address);
        return val != null ? val : IntSet.of();
    }

    public boolean hasEffect(Entity entity, Effect effect) {
        return hasEffect(entity, effect.id);
    }

    public boolean hasEffect(Entity entity, int effect) {
        return getEffects(entity).contains(effect);
    }

    private static class EffectIntSet extends IntArraySet {
        private int ticksSinceAdd;

        public boolean clearOrExpire() {
            if (ticksSinceAdd++ > 100) {
                return true;
            } else {
                super.clear();
                return false;
            }
        }

        @Override
        public boolean add(int k) {
            ticksSinceAdd = 0;
            return super.add(k);
        }
    }

    /**
     * @deprecated use eu.darkbot.api.game.enums.EntityEffect instead
     */
    @Deprecated
    @Getter
    public enum Effect {
        UNDEFINED(-1),
        LOCATOR(1),
        PET_SPAWN(2),
        SOLACE_NANO_CLUSTER(3),
        DIMINISHER_WEAKEN_SHIELDS(4),
        SPECTRUM_PRISMATIC_SHIELD(5),
        SENTINEL_FORTRESS(6),
        /** Cyborg uses the same effect as venom for singularity */
        VENOM_SINGULARITY(7),
        /** When slowed down by saboteur or DCR-250 and similar */
        SHIP_SLOWDOWN(9),
        /* SHIELD_1(10), */
        ENERGY_LEECH(11),
        REPAIR_BATTLE_BOT(12),
        /* LEVEL_UP(13), */
        LIGHTNING_ABILITY(14),
        /** Hammerclaw or pet healing can use the same effect */
        AEGIS_HEAL_RAY(15),
        NPC_ISH(16),
        /* RAGE(17),
        SKULL(18),
        SPAWN(19), */
        BOX_COLLECTING(20),
        BOOTY_COLLECTING(21),
        /* STATIC_MAP(22),
        ACHIEVEMENT(23),
        SPAWN_CHILDREN(24),
        IS_SPAWNED(25),
        UBER(26),
        ABSORBATION(27),
        ABSORBATION_ELITE(28), */
        LEONOV_HOME_BONUS(29),
        SHIELD_ENGINEERING_SKILL(30),
        /** Hammerclaw healing can use the same effect */
        AEGIS_SHIELD_RAY(31),
        /* MARKED_BY_BEACON(32),
        STANDALONE_RING_POD(33),
        DAMAGE_TRANSFER_RAY(34), */
        CITADEL_FORTIFY(35),
        /** CITADEL_DRAW_FIRE */
        DRAW_FIRE(36),
        CITADEL_PROTECTION(37),
        /** A nearby citadel is casting protection on this ship */
        CITADEL_PROTECTED(38),
        CITADEL_DRAW_FIRE_VICTIM(39),
        /* MOUSE_CURSOR_CHANGE(40), */
        /** Animation when changing ship */
        WARP_ANIMATION(41),
        /* INVASION_NPC(42),
        SOLAR_GLOW(43),
        SATURN_EVIL_GLOW(44),*/
        /** CBS deflector shield, prevents it being attacked */
        DEFLECTOR_SHIELD(45),
        /*EMERGENCY_REPAIR(46),
        CONSTRUCTION(47),
        SELF_HEAL(48), */
        /** Show a timer on the entity, eg: NPC_NUKE Tech, or CBS timer */
        COOLDOWN_TIMER(49),
        /* MODULE_LEVEL_UP(50),
        GHOST_NPC(51),
        BIG_PUMPKIN_ABILITY_1(52),
        BIG_PUMPKIN_ABILITY_2(53),
        MODULE_INSTALL(54),
        MIRROR_CONTROLS(55), */
        STICKY_BOMB(56),
        STICKY_BOMB_FLYING(57),
        /* GLOWING_RING_EFFECT(58),
        POI_ZONE_EXPLOSION_EFFECT(59),
        POI_ZONE_EXPLOSION_EFFECT_CONTAINER(60),
        PROGRESS_TO_NEUTRAL(61),
        PROGRESS_TO_OCCUPIED_EFFECT(62),
        GENERIC_GLOW_EFFECT(63),
        DECLOAK_EFFECT(64), */
        POLARITY_POSITIVE(65),
        POLARITY_NEGATIVE(66),
        POLARITY(67),
        WARP_POWERUP(68),
        EMPEROR_STICKY_BOMB(69),
        EMPEROR_STICKY_BOMB_FLYING(70),
        EMPEROR_LORDAKIUM_SPAWN_EFFECT(71),
        /* HEAL_EFFECT(72),
        DMG_BUFF(73),
        DMG_DEBUFF(74),
        HIGHLIGHT_MAP_ASSET_EFFECT(75), */
        REPAIR_BOT(76),
        /* DOMINATION_ICON(77),
        CAMERA_DRONE_EFFECT(78),*/
        /** R-IC3 effect for example */
        ICY_CUBE_EFFECT(79),
        MARKED_TARGET_BLUE(80),
        MARKED_TARGET_RED(81),
        /* SHIP_SPAWN(82),
        SHIP_DESPAWN(83), */
        ISH(84),
        /** IM-01 effect for example */
        INFECTION(85),
        INFECTION_CURE(87),
        /* TA_CHAMPION(88),
        DISRUPTION(89),
        STREUNER_TURRET(91), */
        TARTARUS_SPEED_BOOST(92),
        TARTARUS_RAPID_FIRE(93),
        MIMESIS_PHASE_OUT(95),
        PROMETHEUS_SHOT_LOADED(98),
        /* LONG_RANFE_LASER_EFFECT(100),
        MAYHEM_RAGE(287),
        MAYHEM_WINNER(288), */
        PVP_PROTECTION(290),
        BATTLE_REPAIR_BOT2(302),
        HECATE_PARTICLE_BEAM(303),
        /* SHIELD_2(304),
        KILL_STREAL_CLOUD(310),
        KILL_STREAK_TRAIL(312),
        KILL_STREAK_ACTIVATION(313), */
        DISRUPTOR_REDIRECT(320),
        DISRUPTOR_SHIELD_DISARRAY(321),
        DISRUPTOR_DDOL(322),
        BERSERKER_SHIELD_LINK(323),
        BERSERKER_BERSERK(324),
        BERSERKER_REVENGE(325),
        SOLARIS_INCINERATE(327),
        /* VALENTINE_BUFF(328),
        PLAGUE_SPREAD_INFECTED(330),
        CHARGESHOT(332), */
        SPEED_BURST_TECH(333),
        ANTI_MINE_TRANSMITTER_TECH(334),
        /* SHIP_GLOW_EFFECT(335),
        EMERGENCY_RESERVERS(336),
        ASSIMILATE(337),
        FROST_FIELD_1(338),
        PROMETHEUS_KILLSTREAK_CLOUD(338),
        PROMETHEUS_KILLSTREAK_TRAIL(339),
        PROMETHEUS_KILLSTREAK_ACTIVATION(340),
        PVP_MODE(341),
        FROST_FIELD_2(341), */
        HOLO_REVERSAL_SELF(343),
        HOLO_ENEMY_REVERSAL(344),
        CITADEL_PLUS_PRISMATIC_ENDURANCE(345),
        /*APRILS_FOOLS_TRAILS(1337)*/;

        private final int id;

        Effect(int id) {
            this.id = id;
        }
    }
}
