package com.github.manolo8.darkbot.utils;

import java.nio.file.Paths;

public class LibUtils {

    public static void loadLibrary(String library) {
        LibSetup.downloadLib(library + getExtension());
        System.load(getLibPath(library));
    }

    /**
     * @param library Filename of library without extension, e.g. "DarkBoatAPI"
     * @return Absolute path of library with extension based on OS
     */
    public static String getLibPath(String library) {
        return Paths.get("lib", library + getExtension()).toAbsolutePath().toString();
    }

    /**
     * @return Library extension based on OS
     */
    private static String getExtension() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) return ".dll";
        else return ".so";
    }
}
