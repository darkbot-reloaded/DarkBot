package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.impl.config.DefaultHandler;

import java.lang.reflect.Field;

public class NumberHandler extends DefaultHandler<java.lang.Number> {

    private final double min, max;

    public static NumberHandler of(Field field) {
        Number num = field.getAnnotation(Number.class);
        return new NumberHandler(field, num.min(), num.max(), num.step(), false);
    }

    public static NumberHandler ofLegacy(Field field) {
        Num num = field.getAnnotation(Num.class);
        return new NumberHandler(field, num.min(), num.max(), num.step(), false);
    }

    public static NumberHandler ofPercentage(Field field) {
        Number num = field.getAnnotation(Number.class);
        return new NumberHandler(field,
                num != null ? num.min() : 0,
                num != null ? num.max() : 1,
                num != null ? num.step() : 0.05, true);
    }

    public NumberHandler(double min, double max, double step) {
        this(null, min, max, step, false);
    }

    public NumberHandler(double min, double max, double step, boolean percent) {
        this(null, min, max, step, percent);
    }

    public NumberHandler(Field field, double min, double max, double step, boolean percent) {
        super(field);
        metadata.put("min", this.min = min);
        metadata.put("max", this.max = max);
        metadata.put("step", step);
        metadata.put("isPercent", percent);
    }

    @Override
    public java.lang.Number validate(java.lang.Number number) {
        if (number.doubleValue() < min) number = MathUtils.toNumber(min, number.getClass());
        if (number.doubleValue() > max) number = MathUtils.toNumber(max, number.getClass());
        return number;
    }

}
