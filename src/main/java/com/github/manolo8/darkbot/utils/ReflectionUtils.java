package com.github.manolo8.darkbot.utils;

public class ReflectionUtils {

    public static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException("No default constructor found for " + clazz.getName());
        }
    }

}
