package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.config.ConfigManager;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.LogUtils;

import javax.swing.*;

public class Bot {

    private static final StartupParams STARTUP_PARAMS = new StartupParams();

    public static void main(String[] args) {
        if (System.console() == null
                && Bot.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar")) {
            LogUtils.setOutputToFile();
        }
        try {
            UIManager.getFont("Label.font"); // Prevents a linux crash
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 0);
            UIManager.put("Component.arc", 0);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        STARTUP_PARAMS.parse(args);
        SwingUtilities.invokeLater(Main::new);
    }

    public static class NoOpBot {
        public static void main(String[] args) {
            ConfigManager.FORCE_NO_OP = true;
            Bot.main(args);
        }
    }

    public static StartupParams getStartupParams() {
        return STARTUP_PARAMS;
    }
}
