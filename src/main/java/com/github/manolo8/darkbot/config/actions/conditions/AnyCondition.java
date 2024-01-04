package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.ValueData;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "any", description = "Returns true if any child conditions return true", example = "any(a, b)")
public class AnyCondition extends AbstractCondition {

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        return super.getValue(api, 1, children.size());
    }

}
