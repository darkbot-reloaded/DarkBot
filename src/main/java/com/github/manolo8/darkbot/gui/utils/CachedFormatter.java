package com.github.manolo8.darkbot.gui.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CachedFormatter {
    private final String pattern;

    private Object[] cachedArgs;
    private String cachedResult;

    private CachedFormatter(String pattern) {
        this.pattern = pattern;
    }

    public static CachedFormatter ofPattern(@NotNull String pattern) {
        return new CachedFormatter(pattern);
    }

    public String format(Object... args) {
        if (Arrays.equals(cachedArgs, args))
            return cachedResult;

        return cachedResult = String.format(pattern, cachedArgs = args);
    }
}
