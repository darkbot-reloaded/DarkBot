package com.github.manolo8.darkbot.gui.utils;

import java.io.File;

public class Strings {

    public static String fileName(String path) {
        if (path == null || path.isEmpty()) return "-";
        int split = path.lastIndexOf(File.separatorChar);
        return split > 0 ? path.substring(split + 1) : path;
    }

}
