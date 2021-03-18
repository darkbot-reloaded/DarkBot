package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ValueData("equal")
public class EqualCondition implements Condition {

    public Value<Object> a, b;

    @Override
    public @NotNull Result get(Main main) {
        return Result.fromBoolean(Objects.equals(Value.get(a, main), Value.get(b, main)));
    }

    @Override
    public String toString() {
        return "equal(" + a + ", " + b + ")";
    }
}
