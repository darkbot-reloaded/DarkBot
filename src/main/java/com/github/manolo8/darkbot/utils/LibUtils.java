package com.github.manolo8.darkbot.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibUtils {

    public static void loadLibrary(String library) {
        LibSetup.downloadLib(library + getExtension());

        Path libraryPath = getLibraryPath(library);
        if (Files.exists(libraryPath))
            System.load(libraryPath.toString());
        else throw new UnsatisfiedLinkError("Library file does not exists! " + libraryPath);
    }

    public static Path getSharedLibrary(String library) {
        Path lib = OSUtil.getDataPath("lib", library);
        LibSetup.downloadLib(lib);

        return lib;
    }

    /**
     * @param library Filename of library without extension, e.g. "DarkBoatAPI"
     * @return Absolute path of library with extension based on OS
     */
    public static String getLibPath(String library) {
        return Paths.get("lib", library + getExtension()).toAbsolutePath().toString();
    }

    public static Path getLibraryPath(String library) {
        return Paths.get("lib", library + getExtension()).toAbsolutePath();
    }

    /**
     * @return Library extension based on OS
     */
    private static String getExtension() {
        return "." + OSUtil.getCurrentOs().getLibraryExtension();
    }
}
