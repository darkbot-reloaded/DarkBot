package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.core.utils.Location;
import org.jetbrains.annotations.Nullable;

@ValueData(name = "location", description = "Creates a location constant", example = "location(1000,4000)")
public class LocationConstant implements Value<Location>, Parser {

    public Location location;

    @Override
    public @Nullable Location get(Main main) {
        return location;
    }

    @Override
    public String toString() {
        return "location(" + location + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split(" *, *", 2);

        double x = NumberConstant.parseNumber(params[0], str, getClass()).doubleValue();

        params = (str = ParseUtil.separate(params, getClass(), ",")).split("\\)", 2);
        double y = NumberConstant.parseNumber(params[0], str, getClass()).doubleValue();

        location = new Location(x, y);

        return ParseUtil.separate(params, getClass(), ")");
    }

}
