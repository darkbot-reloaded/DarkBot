package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.config.actions.LegacyCondition;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.types.Condition;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "until", description = "Returns true if the first matches until the second one turns true", example = "until(a, b)")
public class UntilCondition implements LegacyCondition {

    public Value<Condition.Result> from, until;

    private transient boolean current = false;

    @Override
    public @NotNull Condition.Result get(PluginAPI api) {
        if (Value.allows(current ? until : from, api)) current = !current;

        return Condition.Result.fromBoolean(current);
    }

    @Override
    public String toString() {
        return "until(" + from + ", " + until + ")";
    }

}
