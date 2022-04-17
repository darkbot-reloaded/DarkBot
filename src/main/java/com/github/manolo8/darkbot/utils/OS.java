package com.github.manolo8.darkbot.utils;

import org.intellij.lang.annotations.RegExp;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

public enum OS {
    WINDOWS("win"),
    MACOS("mac os x|macos"),
    LINUX("linux"),
    UNKNOWN;

    private static OS os;
    private static Path dataPath;

    private final String pattern;

    OS(@RegExp String pattern) {
        this.pattern = pattern;
    }

    OS() {
        this(null);
    }

    public static OS getOS() {
        if (os != null) return os;

        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        for (OS value : values()) {
            if (value.pattern != null
                    && Pattern.compile(value.pattern).matcher(osName).find()) {
                os = value;
                return os;
            }
        }

        return os = UNKNOWN;
    }

    public static Path getDataPath() {
        if (dataPath != null) return dataPath;

        switch (getOS()) {
            case WINDOWS:
                return dataPath = Paths.get(System.getenv("APPDATA"), "DarkBot");
            case MACOS:
                return dataPath = Paths.get(System.getProperty("user.data"), "Library", "Application Support", "DarkBot");
            case LINUX:
                String home = System.getenv("XDG_DATA_HOME");
                if (home != null && !home.isEmpty())
                    return dataPath = Paths.get(home, "DarkBot");

                return dataPath = Paths.get(System.getProperty("user.data"), "/.local/share", "DarkBot");

            default:
                return dataPath = Paths.get("");
        }
    }
}
