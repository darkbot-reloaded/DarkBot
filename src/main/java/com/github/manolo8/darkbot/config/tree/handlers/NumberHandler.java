package com.github.manolo8.darkbot.config.tree.handlers;

import eu.darkbot.api.config.annotations.Number;

import java.lang.reflect.Field;

public class NumberHandler implements ValueHandler<java.lang.Number> {

    private final double min, max, step;

    public NumberHandler(Field field) {
        Number num = field.getAnnotation(Number.class);
        if (num == null) {
            this.min = 0;
            this.max = 100;
            this.step = 5;
        } else {
            this.min = num.min();
            this.max = num.max();
            this.step = num.step();
        }
    }

    public NumberHandler(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
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
