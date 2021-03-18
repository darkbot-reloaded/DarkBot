package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;

import java.text.NumberFormat;
import java.util.Locale;

@ValueData("number")
public class NumberConstant implements Value<Number>, Parser {

    public Number number;

    @Override
    public Number get(Main main) {
        return number;
    }

    @Override
    public String toString() {
        return "number(" + NumberFormat.getNumberInstance(Locale.ROOT).format(number) + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        number = parseNumber(params[0], str);

        if (params.length != 2)
            throw new SyntaxException("Missing end separator in number", str, ")");

        return params[1];
    }

    public static double parseNumber(String val, String ex) throws SyntaxException {
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Failed to parse number '" + val + "'", ex);
        }
    }

}
