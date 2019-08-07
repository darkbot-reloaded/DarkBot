package com.github.manolo8.darkbot.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtils {
    private ReflectionUtils() {};

    /** A map from primitive types to their corresponding wrapper types. */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    static {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>(16);
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(void.class, Void.class);

        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primitiveToWrapper);
    }

    private static Map<Class<?>, Object> SINGLETON_INSTANCES = new HashMap<>();

    public static <T> T createSingleton(Class<T> clazz) {
        //noinspection unchecked
        return (T) SINGLETON_INSTANCES.computeIfAbsent(clazz, ReflectionUtils::createInstance);
    }

    public static <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null, null);
    }

    public static <T, P> T createInstance(Class<T> clazz, Class<P> paramTyp, P param) {
        try {
            if (paramTyp != null) {
                try {
                    return clazz.getConstructor(paramTyp).newInstance(param);
                } catch (NoSuchMethodException ignore) {}
            }
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for " + clazz.getName());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error creating instance of " + clazz.getName(), e);
        }
    }

    public static Object get(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void set(Field field, Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> Class<T> wrapped(Class<T> type) {
        if (!type.isPrimitive()) return type;
        //noinspection unchecked
        return (Class<T>) PRIMITIVE_TO_WRAPPER.get(type);
    }

}
