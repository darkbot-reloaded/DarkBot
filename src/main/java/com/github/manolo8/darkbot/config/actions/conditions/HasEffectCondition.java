package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.core.entities.Ship;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@ValueData(name = "has-effect", description = "Checks if a ship has an effect", example = "has-effect(effect, ship)")
public class HasEffectCondition implements Condition, Parser {

    public Effect effect;
    public Value<Ship> ship;

    @Override
    public @NotNull Condition.Result get(Main main) {
        Ship sh;
        if ((effect == null) || (sh = Value.get(ship, main)) == null) return Result.ABSTAIN;

        return Result.fromBoolean(main.effectManager.hasEffect(sh, effect.id));
    }

    public enum Effect {
        LEECH(11),
        NPC_ISH(16),
        STICKY_BOMB(56),
        ISH(84);

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
