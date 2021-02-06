package com.github.manolo8.darkbot.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

    public static <T> T createInstance(String className, String jarUrl) {
        try {
            URLClassLoader loader = new URLClassLoader(new URL[]{new File(jarUrl).toURI().toURL()});
            return createInstance((Class<T>) loader.loadClass(className));
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find class: " + className);
        }
    }

    public static <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null, null);
    }

    public static <T> T createInstance(Class<T> clazz, Class<?> paramTyp, Object param) {
        try {
            if (paramTyp != null) {
                try {
                    return clazz.getConstructor(paramTyp).newInstance(param);
                } catch (NoSuchMethodException ignore) {}
            }
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("No default constructor found for " + clazz.getName());
        } catch (InvocationTargetException e) {
            e.getCause().getStackTrace();
            throw new RuntimeException("Error creating instance of " + clazz.getName(), e.getCause());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
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

    public static Type[] findGenericParameters(Class<?> clazz, Class<?> generic) {
        Type[] params;
        for (Type itf : clazz.getGenericInterfaces()) {
            if ((params = getTypes(itf, generic)) != null) return params;
        }
        if ((params = getTypes(clazz.getGenericSuperclass(), generic)) != null) return params;

        Class<?> parent = clazz.getSuperclass();
        if (parent != null) return findGenericParameters(generic, parent);
        return null;
    }

    private static Type[] getTypes(Type type, Class<?> expected) {
        if (!(type instanceof ParameterizedType)) return null;
        ParameterizedType paramType = (ParameterizedType) type;
        if (paramType.getRawType() == expected) return paramType.getActualTypeArguments();
        return null;
    }

}
