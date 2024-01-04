package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
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
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(2, getClass());
        double x = NumberConstant.parseNumber(node.getParam(0), getClass()).doubleValue();
        double y = NumberConstant.parseNumber(node.getParam(1), getClass()).doubleValue();

        location = new Location(x, y);
    }

}
