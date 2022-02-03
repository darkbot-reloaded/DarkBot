package com.github.manolo8.darkbot.gui.tree.utils;

import java.awt.event.FocusEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FocusEventUtil {

    private static boolean errored;

    private static Class<?> focusEventClass;
    private static Method getCause;

    static {
        try {
            if (System.getProperty("java.version").startsWith("1.8")) {
                focusEventClass = Class.forName("sun.awt.CausedFocusEvent");
                getCause = focusEventClass.getDeclaredMethod("getCause");
            } else {
                focusEventClass = FocusEvent.class;
                //noinspection JavaReflectionMemberAccess - This was added in java 9, we're compiling against 8
                getCause = FocusEvent.class.getDeclaredMethod("getCause");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errored = true;
        }
    }


    public static boolean isWindowActivation(FocusEvent e) {
        if (errored) return false; // We can't possibly know

        if (!focusEventClass.isInstance(e)) return false;

        try {
            Enum<?> result = (Enum<?>) getCause.invoke(e);
            // Check as string, because they're different enums (CausedFocusEvent.Cause vs FocusEvent.Cause)
            return result != null && result.name().equals("ACTIVATION");
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return false;
    }


}
