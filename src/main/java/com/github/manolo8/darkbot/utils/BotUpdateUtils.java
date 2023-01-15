package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.Bot;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.Popups;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BotUpdateUtils {

    public static void checkAndUpdateBot(String... args) {
        String defaultJarName = "DarkBot.jar";
        String defaultTempJarName = "DarkBot.jar.tmp";

        try {
            String runningNameJar = getRunningNameJar();
            if (runningNameJar == null) return;
            String[] command = prefix(args, "java", "-jar", defaultJarName);

            if (runningNameJar.endsWith(".jar")) {
                new File(defaultTempJarName).delete();

                LibSetup.Lib lib = LibSetup.getLib(defaultJarName);

                if (Main.VERSION.compareTo(lib.version) < 0) {
                    System.out.println("DarkBot has an update from version " + Main.VERSION + " to " + lib.version);
                    int result = Popups.of("DarkBot updater", "A new version of DarkBot is available\n" +
                                    Main.VERSION + " ➜ " + lib.version, JOptionPane.INFORMATION_MESSAGE)
                            .optionType(JOptionPane.OK_CANCEL_OPTION)
                            .showOptionSync();

                    if (result == JOptionPane.OK_OPTION) {
                        InputStream in = new URL(lib.download).openStream();
                        copyAndRun(in, Path.of(defaultTempJarName), command);
                    }
                }
            } else if (runningNameJar.equals(defaultTempJarName)) {
                copyAndRun(Files.newInputStream(Path.of(runningNameJar)), Path.of(defaultJarName), command);
            }
        } catch (InvalidPathException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRunningNameJar() {
        try {
            return Path.of(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String[] prefix(String[] array, String... prefix) {
        String[] newArr = new String[array.length + prefix.length];
        System.arraycopy(prefix, 0, newArr, 0, prefix.length);
        System.arraycopy(array, 0, newArr, prefix.length, array.length);
        return newArr;
    }

    private static void copyAndRun(InputStream newExecutable, Path path, String... args) {
        try {
            Files.copy(newExecutable, path, StandardCopyOption.REPLACE_EXISTING);
            String[] command = prefix(args, "java", "-jar", path.getFileName().toString());
            Runtime.getRuntime().exec(command);
            System.out.println("Closing process, updated jar '" + path + "' started running!");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
