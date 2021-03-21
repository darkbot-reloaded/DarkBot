package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "boolean", description = "Boolean constant", example = "boolean(true)")
public class BooleanConstant implements Condition, Parser {

    private Boolean value;

    @Override
    public @NotNull Result get(Main main) {
        return Result.fromBoolean(value);
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        value = parse(params[0].trim(), str);
        return ParseUtil.separate(params, getClass(), ")");
    }

    @Override
    public String toString() {
        return "boolean(" + value + ")";
    }

    private Boolean parse(String bool, String ex) throws SyntaxException {
        switch (bool) {
            case "null": return null;
            case "true": return true;
            case "false": return false;
            default:
                throw new SyntaxException("Failed to parse boolean '" + bool + "'", ex, Values.getMeta(getClass()), "true", "false");
        }
    }

}
