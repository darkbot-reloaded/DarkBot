package com.github.manolo8.darkbot.utils;

import org.intellij.lang.annotations.Language;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

public class OSUtil {
    private static final OS CURRENT_OS = OS.findCurrentOS();

    public static OS getCurrentOs() {
        return CURRENT_OS;
    }

    public static Path getDataPath() {
        return getCurrentOs().getBotDataPath();
    }

    public static Path getDataPath(String... more) {
        return Paths.get(getDataPath().toString(), more);
    }

    public enum OS {
        WINDOWS("win", Paths.get(System.getenv("APPDATA"))),
        MACOS("mac", Paths.get(System.getProperty("user.data"), "Library", "Application Support")),
        LINUX("nix|nux|aix", Paths.get(System.getProperty("user.data"), ".local", "share")),
        //SOLARIS("sunos"),
        UNKNOWN(null, Paths.get("cache")); //store data in (current folder -> cache)

        private final String pattern;
        private final Path botDataPath;

        OS(@Language("RegExp") String pattern, Path appDataPath) {
            this.pattern = pattern;
            this.botDataPath = appDataPath.resolve("DarkBot");
        }

        private static OS findCurrentOS() {
            String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
            for (OSUtil.OS os : values())
                if (os != null && Pattern.compile(os.pattern).matcher(osName).find())
                    return os;

            System.out.println("Unknown OS! " + osName);
            return UNKNOWN;
        }

        public Path getBotDataPath() {
            return botDataPath;
        }
    }
}
