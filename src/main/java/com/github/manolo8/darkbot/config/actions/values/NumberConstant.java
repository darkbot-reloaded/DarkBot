package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.Values;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@ValueData(name = "number", description = "Creates a number constant", example = "number(1.5)")
public class NumberConstant implements Value<Number>, Parser {

    private static final NumberFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ROOT);
        symbols.setGroupingSeparator('_');

        NUMBER_FORMAT = new DecimalFormat("###,###.##", symbols);
    }

    public Number number;

    @Override
    public Number get(Main main) {
        return number;
    }

    @Override
    public String toString() {
        return "number(" + NUMBER_FORMAT.format(number) + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        number = parseNumber(params[0], str, getClass());
        return ParseUtil.separate(params, getClass(), ")");
    }

    public static Number parseNumber(String val, String str, Class<? extends Value<?>> type) throws SyntaxException {
        if (val.isEmpty())
            throw new SyntaxException("Empty number, add digits", str, Values.getMeta(type));

        try {
            return NUMBER_FORMAT.parse(val);
        } catch (ParseException e) {
            throw new SyntaxException("Failed to parse number '" + val + "'", str, Values.getMeta(type));
        }
    }

    public static String format(Number number) {
        return NUMBER_FORMAT.format(number);
    }

}
