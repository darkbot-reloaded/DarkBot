package com.github.manolo8.darkbot.utils;

public class MathUtils {

    public static final double TAU = Math.PI * 2;
    public static final double HALF_PI = Math.PI * 0.5;

    public static double angleDiff(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % TAU;
        return phi > Math.PI ? TAU - phi : phi;
    }

    public static <T extends Number> T toNumber(Number number, Class<T> type) {
        if (number == null) return null;
        if (number.getClass() == type) return type.cast(number);
        if (type == Byte.class) return type.cast(number.byteValue());
        if (type == Double.class) return type.cast(number.doubleValue());
        if (type == Float.class) return type.cast(number.floatValue());
        if (type == Integer.class) return type.cast(number.intValue());
        if (type == Long.class) return type.cast(number.longValue());
        if (type == Short.class) return type.cast(number.shortValue());
        throw new UnsupportedOperationException(
                "Cannot convert number to type " + type.getName() + ", only boxed primitives are supported!");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> Comparable<T> toComparable(Number number, Class<T> type) {
        return (Comparable<T>) toNumber(number, type);
    }

    public static boolean isPowerOfTen(int value) {
        return (Math.log10(value) % 1.0) == 0;
    }
}