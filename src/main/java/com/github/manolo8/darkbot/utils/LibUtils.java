package com.github.manolo8.darkbot.utils;

import java.nio.file.Paths;

public class LibUtils {
    /**
     * Loads specified library with improved precision of path, goals to avoid loading from unwanted places.
     * @param library Filename of library including extension, e.g. "DarkBoatAPI.dll"
     */
    public static void loadLibrary(String library) {
        System.load(Paths.get("lib", library).toAbsolutePath().toString());
    }
}
