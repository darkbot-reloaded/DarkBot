package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ValueParser;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import com.github.manolo8.darkbot.core.entities.Ship;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@ValueData(name = "has-formation", description = "Checks if a ship has a formation", example = "has-formation(formation, ship)")
public class HasFormationCondition implements LegacyCondition, Parser {

    public Formation formation;
    public Value<Ship> ship;

    @Override
    public @NotNull Condition.Result get(PluginAPI main) {
        Ship sh;
        if ((formation == null) || (sh = Value.get(ship, main)) == null) return Condition.Result.ABSTAIN;

        return Condition.Result.fromBoolean(sh.formationId == formation.id);
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

        public static Formation of(ParsingNode node) {
            String formation = node.getString();
            for (Formation f : Formation.values()) {
                if (f.toString().equals(formation)) return f;
            }
            throw new SyntaxException("Unknown formation: '" + formation + "'", node, Formation.class);
        }
    }

    @Override
    public String toString() {
        return "has-formation(" + formation + ", " + ship + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());

        formation = Formation.of(node.getParam(0));
        ship = ValueParser.parse(node.getParam(1), Ship.class);
    }
}
