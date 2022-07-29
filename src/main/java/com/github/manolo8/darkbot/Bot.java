package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Bot {

    public static void main(String[] args) throws IOException {
        // You can enable hardware acceleration via adding jvm arg: -Dsun.java2d.opengl=True

        if (System.console() == null
                && Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar")) {
            LogUtils.setOutputToFile();
        }
        try {
            UIManager.getFont("Label.font"); // Prevents a linux crash
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 0);
            UIManager.put("Component.arc", 0);
            UIManager.put("Button.default.boldText", false);
            UIManager.put("Table.cellFocusColor", new Color(0, 0, 0, 160));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        LibSetup.setupLibraries();
        StartupParams params = new StartupParams(args);

        checkUniqueInstance(params); //java 9
        checkJavaVersion(params);
        SwingUtilities.invokeLater(() -> new Main(params));
    }

    private static void checkUniqueInstance(StartupParams params) throws IOException {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;

        Path filePath = Paths.get("curr.pid");
        long currentPid = ProcessHandle.current().pid();

        if (Files.exists(filePath)) {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(filePath));

            if (fileContent.isEmpty()) {
                Files.writeString(filePath, "\n" + currentPid, StandardOpenOption.APPEND);
            } else {
                for (int i = 0; i < fileContent.size(); i++) {
                    if (ProcessHandle.of(Long.parseLong(fileContent.get(i))).isPresent()) {
                        Files.writeString(filePath, "\n" + currentPid, StandardOpenOption.APPEND);
                        Popups.showMessageSync("Multiple bot from same folder", new JOptionPane(
                                "You're currently running multiple bot from same folder" + "\n" +
                                "This may can cause crash or unexpected problems.\n" +
                                "Please create new folder for another instance of bot.",
                                JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
                        break;
                    } else {
                        if (i == fileContent.size() - 1)
                            Files.writeString(filePath, String.valueOf(currentPid), StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }
            }
        } else {
            Files.writeString(filePath, String.valueOf(currentPid), StandardOpenOption.CREATE);
        }
    }

    private static void checkJavaVersion(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;
        String java = System.getProperty("java.version");

        if (!java.startsWith("11.") && !java.startsWith("17.") && !java.equals("17")) {
            Popups.showMessageSync("Unsupported java version", new JOptionPane(
                    "You're currently using java version " + java + "\n" +
                    "This version is unsupported and may stop working on future bot releases.\n" +
                    "Please update to java 11 or java 17 to continue using future releases.",
                    JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
        }
    }

}
