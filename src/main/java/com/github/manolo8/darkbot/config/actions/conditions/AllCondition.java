package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.ValueData;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "all", description = "Returns true if all child conditions return true", example = "all(a, b)")
public class AllCondition extends AbstractCondition {

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        return super.getValue(api, children.size(), children.size());
    }

}
