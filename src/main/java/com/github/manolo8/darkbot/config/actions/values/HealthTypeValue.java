package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseResult;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.core.objects.itf.HealthHolder;

import java.util.Locale;
import java.util.function.Function;

@ValueData(name = "hp-type", description = "Gets a certain HP type from a health", example = "hp-type(hp-percent, a)")
public class HealthTypeValue implements Value<Number>, Parser {

    public HealthType healthType;
    public Value<? extends HealthHolder> health;

    public HealthTypeValue() {}

    @Override
    public Number get(Main main) {
        HealthHolder hh = Value.get(health, main);
        return healthType != null && hh != null ? healthType.getter.apply(hh) : null;
    }

    public enum HealthType {
        HP(HealthHolder::getHp),
        SHIELD(HealthHolder::getShield),
        NANO_HULL(HealthHolder::getHull),
        HP_PERCENT(HealthHolder::hpPercent),
        SHIELD_PERCENT(HealthHolder::shieldPercent);

        private final Function<HealthHolder, Number> getter;

        HealthType(Function<HealthHolder, Number> getter) {
            this.getter = getter;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static HealthType of(String healthType) {
            for (HealthType ht : HealthType.values()) {
                if (ht.toString().equals(healthType)) return ht;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "hp-type(" + healthType + ", " + health + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);
        healthType = HealthType.of(params[0].trim());
        if (healthType == null)
            throw new SyntaxException("Unknown hp-type: '" + params[0] + "'", str, HealthType.class);

        if (params.length != 2)
            throw new SyntaxException("Missing separator in hp-type", "", Values.getMeta(getClass()), ",");

        ParseResult<HealthHolder> pr = ValueParser.parse(params[1], HealthHolder.class);
        health = pr.value;

        str = pr.leftover.trim();
        if (str.isEmpty() || str.charAt(0) != ')')
            throw new SyntaxException("Missing end separator in hp-type", str, Values.getMeta(getClass()), ")");

        return pr.leftover.substring(1);
    }
}
