package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
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

    private Stats.General getKeyFromString(String key) {
        for (Stats.General stat : Stats.General.values()) {
            if (stat.name().equalsIgnoreCase(key)) return stat;
        }
        return null;
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

        public static StatData of(String sd) {
            for (StatData statData : StatData.values()) {
                if (statData.toString().equalsIgnoreCase(sd)) return statData;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "stat-type(" + key.name().toLowerCase(Locale.ROOT) + "," + dataType + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);

        key = getKeyFromString(params[0].trim());
        stat = null;

        if (key == null) {
            throw new SyntaxException("Unknown stat-type: '" + params[0] + "'", str, Stats.General.class);
        }

        params = ParseUtil.separate(params, getClass(), ",").split("\\)", 2);

        dataType = StatData.of(params[0].trim());

        if (dataType == null) {
            throw new SyntaxException("Unknown data-type: '" + params[0] + "'", params[0], StatData.class);
        }

        return ParseUtil.separate(params, getClass(), ")");
    }

}