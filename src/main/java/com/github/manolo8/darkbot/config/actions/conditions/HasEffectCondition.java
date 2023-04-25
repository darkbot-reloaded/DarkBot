package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.core.entities.Ship;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@ValueData(name = "has-effect", description = "Checks if a ship has an effect", example = "has-effect(effect, ship)")
public class HasEffectCondition implements LegacyCondition, Parser {

    public Effect effect;
    public Value<Ship> ship;

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        Ship sh;
        if ((effect == null) || (sh = Value.get(ship, api)) == null) return Condition.Result.ABSTAIN;

        return Condition.Result.fromBoolean(sh.hasEffect(effect.id));
    }

    public enum Effect {
        SOLACE_NANO_CLUSTER(3),
        DIMINISHER_WEAKEN_SHIELDS(4),
        SPECTRUM_PRISMATIC_SHIELD(5),
        SENTINEL_FORTRESS(6),
        /** Cyborg uses the same effect as venom for singularity */
        VENOM_SINGULARITY(7),
        /** When slowed down by saboteur or DCR-250 and similar */
        SHIP_SLOWDOWN(9),
        LEECH(11),
        LIGHTNING_ABILITY(14),
        NPC_ISH(16),
        CITADEL_FORTIFY(35),
        CITADEL_DRAW_FIRE(36),
        CITADEL_PROTECTION(37),
        CITADEL_PROTECTED(38),
        CITADEL_DRAW_FIRE_VICTIM(39),
        STICKY_BOMB(56),
        ISH(84),
        TARTARUS_SPEED_BOOST(92),
        TARTARUS_RAPID_FIRE(93),
        MIMESIS_PHASE_OUT(95),
        PROMETHEUS_SHOT_LOADED(98),
        DISRUPTOR_REDIRECT(320),
        DISRUPTOR_SHIELD_DISARRAY(321),
        DISRUPTOR_DDOL(322),
        BERSERKER_SHIELD_LINK(323),
        BERSERKER_BERSERK(324),
        BERSERKER_REVENGE(325),
        SOLARIS_INCINERATE(327),
        HOLO_REVERSAL_SELF(343),
        HOLO_ENEMY_REVERSAL(344),
        CITADEL_PLUS_PRISMATIC_ENDURANCE(345);

        private final int id;

        Effect(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static Effect of(String operation) {
            for (Effect ef : Effect.values()) {
                if (ef.toString().equals(operation)) return ef;
            }
            return null;
        }

    }

    @Override
    public String toString() {
        return "has-effect(" + effect + ", " + ship + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);
        effect = Effect.of(params[0].trim());
        if (effect == null)
            throw new SyntaxException("Unknown effect: '" + params[0] + "'", str, Effect.class);

        str = ParseUtil.separate(params, getClass(), ",");

        ParseResult<Ship> pr = ValueParser.parse(str, Ship.class);
        ship = pr.value;

        return ParseUtil.separate(pr.leftover.trim(), getClass(), ")");
    }
}
