package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "boolean", description = "Boolean constant", example = "boolean(true)")
public class BooleanConstant implements LegacyCondition, Parser {

    private Boolean value;

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        return Condition.Result.fromBoolean(value);
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(1, getClass());
        value = parseBoolean(node.getParam(0));
    }

    @Override
    public String toString() {
        return "boolean(" + value + ")";
    }

    private Boolean parseBoolean(ParsingNode node) throws SyntaxException {
        switch (node.getString()) {
            case "null": return null;
            case "true": return true;
            case "false": return false;
            default:
                throw new SyntaxException("Failed to parse boolean '" + node.getString() + "'", node, Values.getMeta(getClass()), "true", "false");
        }
    }

}
