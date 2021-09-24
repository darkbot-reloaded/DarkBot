package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.Config;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class RangeHandler extends DefaultHandler<PercentRange> {

    public static RangeHandler of(Field field) {
        return new RangeHandler(field);
    }

    public RangeHandler() {
        this(null);
    }

    public RangeHandler(@Nullable Field field) {
        super(field);
    }

    @Override
    public PercentRange validate(PercentRange value) {
        if (value.getMin() <= value.getMax()) return value;
        return new Config.PercentRange(value.getMax(), value.getMin());
    }
}
