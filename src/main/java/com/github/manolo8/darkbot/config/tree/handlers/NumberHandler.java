package com.github.manolo8.darkbot.config.tree.handlers;

import com.github.manolo8.darkbot.config.types.Num;
import eu.darkbot.api.config.annotations.Number;
import eu.darkbot.impl.config.DefaultHandler;

import java.lang.reflect.Field;

public class NumberHandler extends FieldDefaultHandler<java.lang.Number> {

    private final double min, max, step;

    public NumberHandler(Field field) {
        super(field);
        Number num = field.getAnnotation(Number.class);
        this.min = num.min();
        this.max = num.max();
        this.step = num.step();
    }

    public static NumberHandler ofLegacyAnnotation(Field field) {
        Num num = field.getAnnotation(Num.class);
        return new NumberHandler(field, num.min(), num.max(), num.step());
    }

    public NumberHandler(Field field, double min, double max, double step) {
        super(field);

        this.min = min;
        this.max = max;
        this.step = step;
    }

    public NumberHandler(double min, double max, double step) {
        this(null, min, max, step);
    }

    @Override
    public java.lang.Number validate(java.lang.Number number) {
        if (number.doubleValue() < min) number = min;
        if (number.doubleValue() > max) number = max;
        return number;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
