package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.ValueData;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@ValueData("any")
public class AnyCondition extends AbstractCondition {

    @Override
    public @NotNull Result getValue(Main main) {
        return super.getValue(main, 1, children.size());
    }

}
