package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.components.ExitConfirmation;
import com.github.manolo8.darkbot.gui.titlebar.MainTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private final JPanel mainPanel = new JPanel();
    private ExitConfirmation exitConfirmation;
    private MapDrawer mapDrawer;

    public static final Image ICON = UIUtils.getImage("icon");
    public static final int DEFAULT_WIDTH = 640, DEFAULT_HEIGHT = 480;

    public MainGui(Main main) throws HeadlessException {
        super("DarkBot");

        this.configGui = new ConfigGui(main);
        configGui.setIconImage(ICON);

        this.main = main;

        ToolTipManager.sharedInstance().setInitialDelay(350);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Config.BotSettings.BotGui guiConfig = main.config.BOT_SETTINGS.BOT_GUI;
        WindowUtils.setWindowSize(this, guiConfig.SAVE_GUI_POS, guiConfig.MAIN_GUI_WINDOW);

        setIconImage(ICON);

        setComponentPosition();

        WindowUtils.setupUndecorated(this, mainPanel);
        setVisible(true);

        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(main.config.BOT_SETTINGS.BOT_GUI.ALWAYS_ON_TOP);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> main.configManager.saveConfig()));
    }

    private void setComponentPosition() {
        mainPanel.setLayout(new MigLayout("ins 0, gap 0, wrap 1, fill", "[]", "[][][grow]"));
        mainPanel.add(new MainTitleBar(main, this), "grow, span");
        mainPanel.add(exitConfirmation = new ExitConfirmation(), "grow, span, hidemode 2");
        mainPanel.add(mapDrawer = new MapDrawer(main), "grow, span");
    }

    public void addConfigVisibilityListener(Consumer<Boolean> listener) {
        configGui.addVisibilityListener(listener);
    }

    public void toggleConfig() {
        boolean open = !configGui.isVisible();
        configGui.setVisible(open);
        if (open) {
            configGui.setAlwaysOnTop(this.isAlwaysOnTop());
            configGui.toFront();
        }
    }

    public void setCustomConfig(String name, Object config) {
        configGui.setCustomConfig(name, config);
    }

    public void updateConfiguration() {
        mapDrawer.setup(main);
        configGui.setComponentData();
    }

    public void tryClose() {
        if (main.config.BOT_SETTINGS.BOT_GUI.CONFIRM_EXIT) exitConfirmation.setVisible(true);
        else {
            System.out.println("Exit button pressed, exiting");
            System.exit(0);
        }
    }

    public void tick() {
        mapDrawer.repaint();
    }

}
