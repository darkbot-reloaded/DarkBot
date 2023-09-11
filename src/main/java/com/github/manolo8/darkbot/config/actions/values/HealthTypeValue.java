package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
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
        HP_PERCENT(HealthHolder::hpPercent),
        SHIELD_PERCENT(HealthHolder::shieldPercent),
        HP(HealthHolder::getHp),
        SHIELD(HealthHolder::getShield),
        NANO_HULL(HealthHolder::getHull);

        private final Function<HealthHolder, Number> getter;

        HealthType(Function<HealthHolder, Number> getter) {
            this.getter = getter;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static HealthType of(ParsingNode node) {
            String healthType = node.getString();
            for (HealthType ht : HealthType.values()) {
                if (ht.toString().equals(healthType)) return ht;
            }
            throw new SyntaxException("Unknown hp-type: '" + healthType + "'", node, HealthType.class);
        }
    }

    @Override
    public String toString() {
        return "hp-type(" + healthType + ", " + health + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());
        healthType = HealthType.of(node.getParam(0));
        health = ValueParser.parse(node.getParam(1), HealthHolder.class);
    }
}
