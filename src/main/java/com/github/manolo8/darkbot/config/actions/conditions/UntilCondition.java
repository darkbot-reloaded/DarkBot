package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "until", description = "Returns true if the first matches until the second one turns true", example = "until(a, b)")
public class UntilCondition implements Condition {

    public Value<Result> from, until;

    private transient boolean current = false;

    @Override
    public @NotNull Condition.Result get(Main main) {
        if (Value.allows(current ? until : from, main)) current = !current;

        return Result.fromBoolean(current);
    }


    @Override
    public String toString() {
        return "until(" + from + ", " + until + ")";
    }

}
