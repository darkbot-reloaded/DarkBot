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
        try {
            String fileName = Path.of(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getFileName().toString();

            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(" ").append("-").append(arg);
            }

            if (fileName.endsWith(".jar")) {
                new File("DarkBot.jar.tmp").delete();

                LibSetup.Lib lib = LibSetup.getLib("DarkBot.jar");

                if (Main.VERSION.compareTo(lib.version) < 0) {
                    System.out.println("DarkBot has an update from version " + Main.VERSION + " to " + lib.version);
                    int result = Popups.of("DarkBot updater", "A new version of DarkBot is available\n" +
                                    Main.VERSION + " ➜ " + lib.version, JOptionPane.INFORMATION_MESSAGE)
                            .optionType(JOptionPane.OK_CANCEL_OPTION)
                            .showOptionSync();

                    if (result == JOptionPane.OK_OPTION) {
                        InputStream in = new URL(lib.download).openStream();
                        Files.copy(in, Path.of("DarkBot.jar.tmp"), StandardCopyOption.REPLACE_EXISTING);

                        Runtime.getRuntime().exec("java -jar DarkBot.jar.tmp" + sb);
                        System.exit(0);
                    }
                }
            }

            if (fileName.equals("DarkBot.jar.tmp")) {
                Files.copy(Path.of(fileName), Path.of("DarkBot.jar"), StandardCopyOption.REPLACE_EXISTING);
                Runtime.getRuntime().exec("java -jar DarkBot.jar" + sb);
                System.exit(0);
            }
        } catch (URISyntaxException | InvalidPathException | IOException e) {
            e.printStackTrace();
        }
    }
}
