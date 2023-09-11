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
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.other.EntityInfo.Diplomacy;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@ValueData(name = "has-relation", description = "Checks the target type", example = "has-relation(npc, target())")
public class TargetTypeCondition implements LegacyCondition, Parser {
    private TargetType type;
    private Value<Ship> ship;

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        if (type == null) {
            return Condition.Result.ABSTAIN;
        }

        return Condition.Result.fromBoolean(matches(Value.get(ship, api)));
    }

    private boolean matches(Ship target) {
        switch (type) {
            case NO_TARGET:
                return target == null || !target.isValid();
            case NPC:
                return target instanceof Npc;
            case ENEMY:
                return target != null && target.isValid() && target.getEntityInfo().isEnemy();
            case ALLIED:
                return target != null && target.isValid()
                        && target.getEntityInfo().getClanDiplomacy() == Diplomacy.ALLIED;
            case NOT_ATTACK_PACT:
                return target != null && target.isValid()
                        && target.getEntityInfo().getClanDiplomacy() == Diplomacy.NOT_ATTACK_PACT;
            default:
                throw new IllegalStateException("Unknown type " + type);
        }
    }

    public enum TargetType {
        NO_TARGET,
        NPC,
        ENEMY,
        ALLIED,
        NOT_ATTACK_PACT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static TargetType of(ParsingNode node) {
            String targetType = node.getString();
            for (TargetType tt : TargetType.values()) {
                if (tt.toString().equals(targetType)) return tt;
            }
            throw new SyntaxException("Unknown type: '" + targetType + "'", node, TargetType.class);
        }
    }

    @Override
    public String toString() {
        return "has-relation(" + type + ", " + ship + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());
        type = TargetType.of(node.getParam(0));
        ship = ValueParser.parse(node.getParam(1), Ship.class);
    }
}