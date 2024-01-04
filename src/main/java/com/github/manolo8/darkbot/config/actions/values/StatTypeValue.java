package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import eu.darkbot.api.game.stats.Stats;
import eu.darkbot.api.managers.StatsAPI;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ValueData(name = "stat-type", description = "Gets a certain Stat type from a bot", example = "stat-type(experience, earned)")
public class StatTypeValue implements Value<Number>, Parser {
    private Stats.General key;
    private StatsAPI.Stat stat;
    private StatData dataType;

    @Override
    public @Nullable Number get(Main main) {
        if (stat == null && key != null) stat = main.statsManager.getStat(key);
        if (dataType == null || key == null || stat == null) {
            return null;
        }

        switch (dataType) {
            case INITIAL: return stat.getInitial();
            case CURRENT: return stat.getCurrent();
            case EARNED: return stat.getEarned();
            case SPENT: return stat.getSpent();
            case DIFFERENCE: return stat.getEarned() - stat.getSpent();
            default: throw new IllegalStateException("Undefined operation " + dataType);
        }
    }

    private Stats.General getKeyFromString(ParsingNode node) {
        String key = node.getString();
        for (Stats.General stat : Stats.General.values()) {
            if (stat.name().equalsIgnoreCase(key)) return stat;
        }
        throw new SyntaxException("Unknown stat-type: '" + key + "'", node, Stats.General.class);
    }

    public enum StatData {
        INITIAL,
        CURRENT,
        EARNED,
        SPENT,
        DIFFERENCE;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static StatData of(ParsingNode node) {
            String statData = node.getString();
            for (StatData sd : StatData.values()) {
                if (sd.toString().equalsIgnoreCase(statData)) return sd;
            }
            throw new SyntaxException("Unknown stat data: '" + statData + "'", node, StatData.class);
        }
    }

    @Override
    public String toString() {
        return "stat-type(" + key.name().toLowerCase(Locale.ROOT) + "," + dataType + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());

        key = getKeyFromString(node.getParam(0));
        dataType = StatData.of(node.getParam(1));
        stat = null;
    }

}