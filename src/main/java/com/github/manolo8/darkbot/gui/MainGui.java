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
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

        Config.BotSettings botSettings = main.config.BOT_SETTINGS;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (botSettings.SAVE_MAIN_GUI_POS_AND_SIZE)
                main.configManager.saveConfig();
        }));

        Config.BotSettings.Window thisWindow = botSettings.MAIN_GUI_WINDOW;
        if (!botSettings.SAVE_MAIN_GUI_POS_AND_SIZE ||
                (thisWindow.x == Integer.MIN_VALUE && thisWindow.y == Integer.MIN_VALUE) ||
                isOutsideScreen(thisWindow)) {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocationRelativeTo(null);
        } else {
            setSize(thisWindow.width, thisWindow.height);
            setLocation(thisWindow.x, thisWindow.y);
        }

        setIconImage(ICON);

        setComponentPosition();

        WindowUtils.setupUndecorated(this, mainPanel);
        setVisible(true);

        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(main.config.BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP);
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
        if (main.config.BOT_SETTINGS.CONFIRM_EXIT) exitConfirmation.setVisible(true);
        else {
            System.out.println("Exit button pressed, exiting");
            System.exit(0);
        }
    }

    public void tick() {
        mapDrawer.repaint();
    }

    private static boolean isOutsideScreen(Config.BotSettings.Window window) {
        return anyScreenDeviceMatches(device -> {
            Rectangle deviceBounds = device.getDefaultConfiguration().getBounds();
            return (window.height > deviceBounds.height || window.width > deviceBounds.width) ||
                    !device.getDefaultConfiguration().getBounds()
                            .intersects(new Rectangle(window.x, window.y, window.width, window.height));
        });
    }

    private static boolean anyScreenDeviceMatches(Predicate<GraphicsDevice> filter) {
        if (filter == null) return false;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return Arrays.stream(ge.getScreenDevices()).anyMatch(filter);
    }

}
