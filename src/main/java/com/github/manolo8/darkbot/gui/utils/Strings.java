package com.github.manolo8.darkbot.gui.utils;

import java.io.File;

public class Strings {

    public static String fileName(String path) {
        if (path == null || path.isEmpty()) return "-";
        int split = path.lastIndexOf(File.separatorChar);
        return split > 0 ? path.substring(split + 1) : path;
    }

    public static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String toTooltip(String str) {
        if (str != null && str.trim().isEmpty()) return null;
        return str;
    }

}
