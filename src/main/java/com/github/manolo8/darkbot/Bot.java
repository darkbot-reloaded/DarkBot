package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.utils.LibSetup;
import com.github.manolo8.darkbot.utils.LogUtils;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

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

        checkUniqueInstance(params);
        checkJavaVersion(params);
        SwingUtilities.invokeLater(() -> new Main(params));
    }

    private static void checkUniqueInstance(StartupParams params) {
        if (params.has(StartupParams.LaunchArg.NO_WARN)) return;

        File path = new File("./");
        File[] listFiles = path.listFiles(file -> file.getName().contains("pid"));
        String currentPid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        if (listFiles.length != 0) {
            // Pid file exists
            String filePid = listFiles[0].getName().split("_")[1];
            try {
                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec("cmd /c tasklist /FI \"PID eq " + filePid + "\"");

                InputStreamReader isReader = new InputStreamReader(pr.getInputStream());
                BufferedReader bReader = new BufferedReader(isReader);
                String strLine;
                while ((strLine = bReader.readLine()) != null) {
                    if (strLine.contains(filePid)) {
                        break;
                    }
                }

                if (strLine != null && strLine.contains(filePid)) {
                    Popups.showMessageSync("Running from same folder detect", new JOptionPane(
                            "You're currently running multiple bot from same folder" + "\n" +
                            "This may can cause crash or unexpected problems.\n" +
                            "Please create new folder for another instance of bot.",
                            JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION));
                } else {
                    listFiles[0].renameTo(new File("pid_" + currentPid));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Pid file not exit
            File pidFile = new File("pid_" + currentPid);
            try {
                pidFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
