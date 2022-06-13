package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

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

        checkJavaVersion(params);
        SwingUtilities.invokeLater(() -> new Main(params));
    }

    private static void checkJavaVersion(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;
        String java = System.getProperty("java.version");

        if (!java.startsWith("11.") && !java.startsWith("17.")) {
            Popups.showMessageSync("Unsupported java version", new JOptionPane(
                    "You're currently using java version " + java + "\n" +
                    "This version is unsupported and may stop working on future bot releases.\n" +
                    "Please update to java 11 or java 17 to continue using future releases.",
                    JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
        }
    }

}
