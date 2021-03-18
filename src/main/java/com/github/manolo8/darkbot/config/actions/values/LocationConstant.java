package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.core.utils.Location;
import org.jetbrains.annotations.Nullable;

@ValueData("location")
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
        String[] params = str.split(" *[,)] *", 3);

        double x = NumberConstant.parseNumber(params[0], str);
        if (params.length < 2) throw new SyntaxException("Missing separator in location", str, ",");

        double y = NumberConstant.parseNumber(params[1], str);
        if (params.length < 3) throw new SyntaxException("Missing end separator in location", str, ")");

        location = new Location(x, y);

        return params[2];
    }

}
