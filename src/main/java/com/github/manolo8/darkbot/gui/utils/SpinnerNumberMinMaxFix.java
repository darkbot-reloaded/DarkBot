package com.github.manolo8.darkbot.gui.utils;

import javax.swing.*;

public class SpinnerNumberMinMaxFix extends SpinnerNumberModel {

    public SpinnerNumberMinMaxFix(Number value, Comparable minimum, Comparable maximum, Number stepSize) {
        super(value, minimum, maximum, stepSize);
    }

    public SpinnerNumberMinMaxFix(int value, int minimum, int maximum, int stepSize) {
        super(value, minimum, maximum, stepSize);
    }

    public SpinnerNumberMinMaxFix(double value, double minimum, double maximum, double stepSize) {
        super(value, minimum, maximum, stepSize);
    }

    @Override
    public Object getNextValue() {
        Object nextVal = super.getNextValue();
        return nextVal == null ? getMaximum() : nextVal;
    }

    @Override
    public Object getPreviousValue() {
        Object prevVal = super.getPreviousValue();
        return prevVal == null ? getMinimum() : prevVal;
    }

    public double getDouble() {
        return getNumber().doubleValue();
    }

}
