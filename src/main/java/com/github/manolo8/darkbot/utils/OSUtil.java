package com.github.manolo8.darkbot.utils;

import org.intellij.lang.annotations.Language;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class OSUtil {
    private static final OS CURRENT_OS = OS.getCurrent();

    public static OS getCurrentOs() {
        return CURRENT_OS;
    }

    public static Path getDataPath() {
        return getCurrentOs().getBotDataPath();
    }

    public static Path getDataPath(String first, String... more) {
        return getDataPath().resolve(Paths.get(first, more));
    }

    // assume that on failure is not a Windows 7
    public static boolean isWindows7OrLess() {
        if (getCurrentOs() != OS.WINDOWS) return false;

        String osVersion = System.getProperty("os.version");
        if (osVersion == null || osVersion.isEmpty()) return false;

        try {
            double version = Double.parseDouble(osVersion);
            return version <= 6.1; // Windows 7 version
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse os version! " + osVersion);
            return false;
        }
    }

    public enum OS {
        WINDOWS("win", "dll", () -> Paths.get(System.getenv("APPDATA"))),
        MACOS("mac", "so", () -> Paths.get(System.getProperty("user.home"), "Library", "Application Support")),
        LINUX("nix|nux|aix", "so", () -> Paths.get(System.getProperty("user.home"), ".local", "share")),
        //SOLARIS("sunos"),
        UNKNOWN(null, null, () -> Paths.get("cache")); //store data in (current folder -> cache)

        private final String pattern, libExtension;
        private final Supplier<Path> appDataPathSupplier;

        OS(@Language("RegExp") String pattern, String libExtension, Supplier<Path> appDataPathSupplier) {
            this.pattern = pattern;
            this.libExtension = libExtension;
            this.appDataPathSupplier = appDataPathSupplier;
        }

        private static OS getCurrent() {
            String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            for (OSUtil.OS os : values())
                if (os != null && Pattern.compile(os.pattern).matcher(osName).find())
                    return os;

            System.out.println("Unknown OS! " + osName);
            return UNKNOWN;
        }

        public Path getBotDataPath() {
            return appDataPathSupplier.get().resolve("DarkBot");
        }

        public String getLibraryExtension() {
            return libExtension;
        }
    }
}
