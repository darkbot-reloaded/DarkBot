package com.github.manolo8.darkbot.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.gui.components.ExitConfirmation;
import com.github.manolo8.darkbot.gui.titlebar.MainTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import eu.darkbot.api.config.ConfigSetting;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private MainTitleBar titleBar;
    private ExitConfirmation exitConfirmation;
    private MapDrawer mapDrawer;

    public static final Image ICON = UIUtils.getImage("icon");
    public static final int DEFAULT_WIDTH = 640, DEFAULT_HEIGHT = 480;
    private int lastTick;

    public MainGui(Main main) throws HeadlessException {
        super("DarkBot");
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, false);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_TITLE, false);

        this.configGui = new ConfigGui(main);
        configGui.setIconImage(ICON);

        this.main = main;

        ToolTipManager.sharedInstance().setInitialDelay(350);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Config.BotSettings.BotGui guiConfig = main.config.BOT_SETTINGS.BOT_GUI;
        WindowUtils.setWindowSize(this, guiConfig.SAVE_GUI_POS, guiConfig.MAIN_GUI_WINDOW);

        setIconImage(ICON);

        setComponentPosition();
        titleBar.setInfo("DarkBot " + Main.VERSION);

        setVisible(true);

        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(main.config.BOT_SETTINGS.BOT_GUI.ALWAYS_ON_TOP);

        if (Main.API.hasCapability(GameAPI.Capability.WINDOW_POSITION)) {
            addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    if (main.config.BOT_SETTINGS.API_CONFIG.attachToBot)
                        Main.API.setPosition((int) getBounds().getMaxX() - 15, (int) getBounds().getMinY());
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                main.pluginHandler.PLUGIN_CLASS_LOADER.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            main.configManager.saveConfig();
        }));
    }

    private void setComponentPosition() {
        setJMenuBar(titleBar = new MainTitleBar(main, this));

        setLayout(new BorderLayout());
        add(exitConfirmation = new ExitConfirmation(), BorderLayout.NORTH);
        add(mapDrawer = new MapDrawer(main), BorderLayout.CENTER);
    }

    public void addConfigVisibilityListener(Consumer<Boolean> listener) {
        configGui.addVisibilityListener(listener);
    }

    public void toggleConfig() {
        boolean open = !configGui.isVisible();
        configGui.setVisible(open);
        if (open) {
            configGui.setState(NORMAL); // bring the window if was minimized
            configGui.setAlwaysOnTop(this.isAlwaysOnTop());
            configGui.toFront();
        }
    }

    public void setCustomConfig(@Nullable ConfigSetting.Parent<?> config) {
        configGui.setCustomConfig(config);
    }

    public void updateConfigTreeListeners() {
        configGui.updateConfigTreeListeners();
    }

    public void updateConfiguration() {
        mapDrawer.setup(main);
        configGui.setComponentData();
    }

    @Override
    public void setTitle(String title) {
        if (!Objects.equals(getTitle(), title)) super.setTitle(title);
    }

    public void tick() {
        if (main.config.MISCELLANEOUS.USERNAME_ON_TITLE) {
            if (main.hero.playerInfo.username != null) setTitle("DarkBot - " + main.hero.playerInfo.username);
        }
        else setTitle("DarkBot");

        if ((lastTick++ % main.config.BOT_SETTINGS.MAP_DISPLAY.REFRESH_DELAY) == 0) {
            mapDrawer.repaint();
        }
    }

    @Override
    protected void processWindowEvent(final WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            // if is minimized close without confirmation
            if (main.config.BOT_SETTINGS.BOT_GUI.CONFIRM_EXIT && getState() == NORMAL) {
                toFront(); // bring to front if possible
                exitConfirmation.setVisible(true);
            }
            else {
                System.out.println("Exit button pressed, exiting");
                System.exit(0);
            }
        }
    }

}
