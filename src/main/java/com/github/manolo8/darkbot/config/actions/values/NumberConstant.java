package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;

@ValueData("number")
public class NumberConstant implements Value<Number>, Parser {

    public Number number;

    @Override
    public Number getValue(Main main) {
        return number;
    }

    @Override
    public String toString() {
        return "number(" + number + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);
        if (params.length != 2)
            throw new SyntaxException("Invalid syntax for number, missing ')'", str);

        try {
            number = Double.parseDouble(params[0]);
        } catch (NumberFormatException e) {
            throw new SyntaxException("Failed to parse number '" + params[0] + "'", str);
        }

        return params[1];
    }

}
