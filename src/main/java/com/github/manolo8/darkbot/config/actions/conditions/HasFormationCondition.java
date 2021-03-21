package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.core.entities.Ship;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@ValueData(name = "has-formation", description = "Checks if a ship has a formation", example = "has-formation(a, b)")
public class HasFormationCondition implements Condition, Parser {

    public Formation formation;
    public Value<Ship> ship;

    @Override
    public @NotNull Condition.Result get(Main main) {
        Ship sh;
        if ((formation == null) || (sh = Value.get(ship, main)) == null) return Result.ABSTAIN;

        return Result.fromBoolean(sh.formationId == formation.id);
    }

    public enum Formation {
        STANDARD,
        TURTLE,
        ARROW,
        LANCE,
        STAR,
        PINCER,
        DOUBLE_ARROW,
        DIAMOND,
        CHEVRON,
        MOTH,
        CRAB,
        HEART,
        BARRAGE,
        BAT,
        RING,
        DRILL,
        VETERAN,
        DOME,
        WHEEL,
        X,
        WAVY,
        MOSQUITO;

        private final int id = ordinal();

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static Formation of(String operation) {
            for (Formation f : Formation.values()) {
                if (f.toString().equals(operation)) return f;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "has-formation(" + formation + ", " + ship + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);
        formation = Formation.of(params[0].trim());
        if (formation == null)
            throw new SyntaxException("Unknown formation: '" + params[0] + "'", str, Formation.class);

        if (params.length != 2)
            throw new SyntaxException("Missing separator in has-formation", "", Values.getMeta(getClass()), ",");

        ParseResult<Ship> pr = ValueParser.parse(params[1], Ship.class);
        ship = pr.value;

        str = pr.leftover.trim();
        if (str.isEmpty() || str.charAt(0) != ')')
            throw new SyntaxException("Missing end separator in has-formation", str, Values.getMeta(getClass()), ")");

        return pr.leftover.substring(1);
    }
}
