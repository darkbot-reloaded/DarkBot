package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class EffectManager implements Manager {
    private long mapAddressStatic;

    private ObjArray effectsPtr = ObjArray.ofVector(true);
    private Map<Long, List<Integer>> effects = new HashMap<>();

    public EffectManager(Main main) {
    }

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> mapAddressStatic = value + 256);
    }

    public void tick() {
        long addr = API.readMemoryLong(API.readMemoryLong(mapAddressStatic), 128, 48);

        effectsPtr.update(addr);
        effects.clear();

        for (int i = 0; i < effectsPtr.getSize(); i++) {
            int id      = API.readMemoryInt( effectsPtr.get(i) + 0x24);
            long entity = API.readMemoryLong(effectsPtr.get(i) + 0x48);

            effects.computeIfAbsent(entity, list -> new ArrayList<>()).add(id);
        }
    }

    public List<Integer> getEffects(Entity entity) {
        if (entity.address == 0) return Collections.emptyList();
        return effects.getOrDefault(entity.address, Collections.emptyList());
    }

    public boolean hasEffect(Entity entity, Effect effect) {
        return hasEffect(entity, effect.id);
    }

    public boolean hasEffect(Entity entity, int effect) {
        return getEffects(entity).contains(effect);
    }

    public enum Effect {
        UNDEFINED(-1),
        LOCATOR(1),
        PET_SPAWN(2),
        SOLACE(3),
        DIMINISHER(4),
        SPECTRUM(5),
        SENTINEL(6),
        VENOM(7),
        SABOTEUR(9),
        SHIELD_1(10),
        ENERGY_LEECH(11),
        REPAIR_BATTLE_BOT(12),
        LEVEL_UP(13),
        HEAL_RAY(15),
        NPC_ISH(16),
        RAGE(17),
        SKULL(18),
        SPAWN(19),
        COLLECTOR_BEAM(20),
        TIMER_COUNTDOWN(21),
        STATIC_MAP(22),
        ACHIEVEMENT(23),
        SPAWN_CHILDREN(24),
        IS_SPAWNED(25),
        UBER(26),
        ABSORBATION(27),
        ABSORBATION_ELITE(28),
        HOME_BONUS(29),
        SHILD_ENGINEERING(30),
        CHARGE_SHIELD_RAY(31),
        MARKED_BY_BEACON(32),
        STANDALONE_RING_POD(33),
        DAMAGE_TRANSFER_RAY(34),
        FORTIFY(35),
        DRAW_FIRE(36),
        PROTECTION(37),
        PROTECTED(38),
        DRAW_FIRE_VICTIM(39),
        MOUSE_CURSOR_CHANGE(40),
        WARP_ANIMATION(41),
        INVASION_NPC(42),
        SOLAR_GLOW(43),
        SATURN_EVIL_GLOW(44),
        DEFLECTOR_SHIELD(45),
        EMERGENCY_REPAIR(46),
        CONSTRUCTION(47),
        SELF_HEAL(48),
        TIME_EFFECT(49),
        MODULE_LEVEL_UP(50),
        GHOST_NPC(51),
        BIG_PUMPKIN_ABILITY_1(52),
        BIG_PUMPKIN_ABILITY_2(53),
        MODULE_INSTALL(54),
        MIRROR_CONTROLS(55),
        STICKY_BOMB(56),
        STICKY_BOMB_FLYING(57),
        GLOWING_RING_EFFECT(58),
        POI_ZONE_EXPLOSION_EFFECT(59),
        POI_ZONE_EXPLOSION_EFFECT_CONTAINER(60),
        PROGRESS_TO_NEUTRAL(61),
        PROGRESS_TO_OCCUPIED_EFFECT(62),
        GENERIC_GLOW_EFFECT(63),
        DECLOAK_EFFECT(64),
        POLARITY_POSITIVE(65),
        POLARITY_NEGATIVE(66),
        POLARITY(67),
        WARP_POWERUP(68),
        EMPEROR_STICKY_BOMB(69),
        EMPEROR_STICKY_BOMB_FLYING(70),
        EMPEROR_LORDAKIUM_SPAWN_EFFECT(71),
        HEAL_EFFECT(72),
        DMG_BUFF(73),
        DMG_DEBUFF(74),
        HIGHLIGHT_MAP_ASSET_EFFECT(75),
        REPAIR_BOT(76),
        DOMINATION_ICON(77),
        CAMERA_DRONE_EFFECT(78),
        ICY_CUBE_EFFECT(79),
        MARKED_TARGET_BLUE(80),
        MARKED_TARGET_RED(81),
        SHIP_SPAWN(82),
        SHIP_DESPAWN(83),
        ISH(84),
        INFECTION(85),
        INFECTION_CURE(87),
        TA_CHAMPION(88),
        DISRUPTION(89),
        STREUNER_TURRET(91),
        RAPID_FIRE(93),
        PHASE_OUT_EFFECT(95),
        LONG_RANFE_LASER_EFFECT(100),
        MAYHEM_RAGE(287),
        MAYHEM_WINNER(288),
        PVP_PROTECTION(290),
        SHIELD_2(304),
        KILL_STREAL_CLOUD(310),
        KILL_STREAK_TRAIL(312),
        KILL_STREAK_ACTIVATION(313),
        DDOL(322),
        BERSERKER_EFFECT(324),
        REVENFE_EFFECT(325),
        INCINERATE_EFFECT(327),
        VALENTINE_BUFF(328),
        PLAGUE_SPREAD_INFECTED(330),
        CHARGESHOT(332),
        SPEED_BURST(333),
        ANTI_MINE_TRANSMITTER(334),
        SHIP_GLOW_EFFECT(335),
        EMERGENCY_RESERVERS(336),
        ASSIMILATE(337),
        FROST_FIELD_1(338),
        PROMETHEUS_KILLSTREAK_CLOUD(338),
        PROMETHEUS_KILLSTREAK_TRAIL(339),
        PROMETHEUS_KILLSTREAK_ACTIVATION(340),
        PVP_MODE(341),
        FROST_FIELD_2(341),
        REVERSAL_SELF(343),
        DISRUPTION_PURPLE(344),
        APRILS_FOOLS_TRAILS(1337);

        private final int id;

        Effect(int id) {
            this.id = id;
        }
    }
}
