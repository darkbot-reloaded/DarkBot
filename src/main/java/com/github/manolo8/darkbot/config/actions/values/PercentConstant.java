package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;

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
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(1, getClass());

        ParsingNode val = node.getParam(0);
        try {
            percent = Double.parseDouble(val.getString()) / 100;
        } catch (NumberFormatException e) {
            throw new SyntaxException("Failed to parse percent '" + val.getFunction() + "'", val, Values.getMeta(getClass()));
        }
    }
}
