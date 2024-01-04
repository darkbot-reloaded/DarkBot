package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.ValueData;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "one", description = "Returns true if exactly one child condition return true", example = "one(a, b)")
public class OneCondition extends AbstractCondition {

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        return super.getValue(api, 1, 1);
    }

}
