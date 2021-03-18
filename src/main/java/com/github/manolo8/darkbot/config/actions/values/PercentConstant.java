package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;

@ValueData("percent")
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

        try {
            percent = Double.parseDouble(params[0]) / 100;
        } catch (NumberFormatException e) {
            throw new SyntaxException("Failed to parse percent '" + params[0] + "'", str);
        }

        if (params.length != 2)
            throw new SyntaxException("Missing end separator in percent", str, ")");

        return params[1];
    }
}
