package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.ValueData;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "none", description = "Returns true if no child conditions return true", example = "none(a, b)")
public class NoneCondition extends AbstractCondition {

    public NoneCondition() {}

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        return super.getValue(api, 0, 0);
    }

}
