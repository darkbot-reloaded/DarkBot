package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.impl.config.DefaultHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class NumberHandler extends DefaultHandler<java.lang.Number> {

    private final double min, max;
    private final Double disabled;

    public static NumberHandler of(Field field) {
        Number num = field.getAnnotation(Number.class);
        Number.Disabled disabled = field.getAnnotation(Number.Disabled.class);
        return new NumberHandler(field, num.min(), num.max(), num.step(), false,
                disabled == null ? null : disabled.value(),
                disabled == null ? null : disabled.def());
    }

    public static NumberHandler ofLegacy(Field field) {
        Num num = field.getAnnotation(Num.class);
        Number.Disabled disabled = field.getAnnotation(Number.Disabled.class);
        return new NumberHandler(field, num.min(), num.max(), num.step(), false,
                disabled == null ? null : disabled.value(),
                disabled == null ? null : disabled.def());
    }

    public static NumberHandler ofPercentage(Field field) {
        Number num = field.getAnnotation(Number.class);
        Number.Disabled disabled = field.getAnnotation(Number.Disabled.class);
        return new NumberHandler(field,
                num != null ? num.min() : 0,
                num != null ? num.max() : 1,
                num != null ? num.step() : 0.05,
                true,
                disabled == null ? null : disabled.value(),
                disabled == null ? null : disabled.def());
    }

    public NumberHandler(double min, double max, double step) {
        this(null, min, max, step, false, null, null);
    }

    public NumberHandler(double min, double max, double step, boolean percent) {
        this(null, min, max, step, percent, null, null);
    }

    public NumberHandler(double min, double max, double step, boolean percent,
                         Double disabled, Double def) {
        this(null, min, max, step, percent, disabled, def);
    }

    public NumberHandler(Field field, double min, double max, double step, boolean percent,
                         @Nullable Double disabled, Double def) {
        super(field);
        metadata.put("min", this.min = min);
        metadata.put("max", this.max = max);
        metadata.put("step", step);
        metadata.put("disabled", this.disabled = disabled);
        metadata.put("disabled-default", def);
        if (percent) metadata.put("isPercent", true);
    }

    @Override
    public java.lang.Number validate(java.lang.Number number) {
        if (disabled != null && number.doubleValue() == disabled) return number;
        return enforceLimit(number, min, max);
    }


    public static java.lang.Number enforceLimit(java.lang.Number number, double min, double max) {
        if (number.doubleValue() < min) number = MathUtils.toNumber(min, number.getClass());
        if (number.doubleValue() > max) number = MathUtils.toNumber(max, number.getClass());
        return number;
    }

}
