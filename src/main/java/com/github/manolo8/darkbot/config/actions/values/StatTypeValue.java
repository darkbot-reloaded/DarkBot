package com.github.manolo8.darkbot.config.actions.values;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;

import eu.darkbot.api.game.stats.Stats;
import eu.darkbot.api.managers.StatsAPI;

@ValueData(name = "stat-type", description = "Gets a certain Stat type from a bot", example = "stat-type(EXPERIENCE, earned)")
public class StatTypeValue implements Value<Number>, Parser {
    private StatsAPI.Key statKey;
    private StatData dataType;

    @Override
    public @Nullable Number get(Main main) {
        if (dataType == null || statKey == null) {
            return null;
        }

        if (dataType == StatData.CURRENT) {
            return main.statsManager.getStat(statKey).getCurrent();
        } else if (dataType == StatData.EARNED) {
            return main.statsManager.getStat(statKey).getEarned();
        } else if (dataType == StatData.SPENT) {
            return main.statsManager.getStat(statKey).getSpent();
        } else if (dataType == StatData.INITIAL) {
            return main.statsManager.getStat(statKey).getInitial();
        }

        return null;
    }

    private StatsAPI.Key getKeyFromString(String key) {
        for (StatsAPI.Key stat : Stats.General.values()) {
            if (stat.name().equals(key)) {
                return stat;
            }
        }

        return null;
    }

    public enum StatData {
        INITIAL,
        CURRENT,
        EARNED,
        SPENT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT).replace("_", "-");
        }

        public static StatData of(String sd) {
            for (StatData statdata : StatData.values()) {
                if (statdata.toString().equals(sd))
                    return statdata;
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "stat-type(" + statKey + "," + dataType + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);

        statKey = getKeyFromString(params[0].trim());

        if (statKey == null) {
            throw new SyntaxException("Unknown stat-type: '" + params[0] + "'", str, Stats.General.class);
        }

        params = (ParseUtil.separate(params, getClass(), ",")).split("\\)", 2);

        dataType = StatData.of(params[0]);

        if (dataType == null) {
            throw new SyntaxException("Unknown data-type: '" + params[0] + "'", params[0], StatData.class);
        }

        return ParseUtil.separate(params, getClass(), ")");
    }

}