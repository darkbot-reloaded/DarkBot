package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import org.jetbrains.annotations.NotNull;

@ValueData("boolean")
public class BooleanConstant implements Condition, Parser {

    private Boolean value;

    @Override
    public @NotNull Result get(Main main) {
        return Result.fromBoolean(value);
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        value = parse(params[0].trim(), params.length == 1 ? "" : params[1]);
        if (params.length != 2)
            throw new SyntaxException("Missing end separator in boolean", str, ")");

        return params[1];
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
                throw new SyntaxException("Failed to parse boolean '" + bool + "'", ex, "true", "false");
        }
    }

}
