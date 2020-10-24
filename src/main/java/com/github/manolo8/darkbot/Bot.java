package com.github.manolo8.darkbot;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.manolo8.darkbot.config.ConfigManager;
import com.github.manolo8.darkbot.utils.StartupParams;
import com.github.manolo8.darkbot.utils.LogUtils;

import javax.swing.*;
import java.io.IOException;

public class Bot {

    public static void main(String[] args) throws IOException {
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
        StartupParams params = args.length == 0 ? null : new StartupParams(args);
        SwingUtilities.invokeLater(() -> new Main(params));
    }

    public static class NoOpBot {
        public static void main(String[] args) throws IOException {
            ConfigManager.FORCE_NO_OP = true;
            Bot.main(args);
        }
    }
}
