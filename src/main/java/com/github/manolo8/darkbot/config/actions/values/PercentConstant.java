package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.Values;

@ValueData(name = "percent", description = "Creates a percent constant", example = "percent(5)")
public class PercentConstant implements Value<Number>, Parser {

    public double percent;

    @Override
    public Number get(Main main) {
        return percent;
    }

    @Override
    public String toString() {
        return "percent(" + (int) (percent * 100) + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        if (params[0].isEmpty())
            throw new SyntaxException("Empty percent, add digits", str, Values.getMeta(getClass()));

        try {
            percent = Double.parseDouble(params[0]) / 100;
        } catch (NumberFormatException e) {
            throw new SyntaxException("Failed to parse percent '" + params[0] + "'", str, Values.getMeta(getClass()));
        }

        return ParseUtil.separate(params, getClass(), ")");
    }
}
