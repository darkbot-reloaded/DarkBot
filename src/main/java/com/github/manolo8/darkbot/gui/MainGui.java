package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.gui.components.ExitConfirmation;
import com.github.manolo8.darkbot.gui.titlebar.MainTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import eu.darkbot.api.config.ConfigSetting;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;
import java.util.function.Consumer;

public class MainGui extends JFrame {

    private final Main main;
    private final ConfigGui configGui;

    private final JPanel mainPanel = new JPanel();
    private MainTitleBar titleBar;
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
        titleBar.setInfo("DarkBot: " + Main.VERSION);

        WindowUtils.setupUndecorated(this, mainPanel);
        setVisible(true);

        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(main.config.BOT_SETTINGS.BOT_GUI.ALWAYS_ON_TOP);

        if (Main.API.hasCapability(GameAPI.Capability.WINDOW_POSITION)) {
            addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    if (main.config.BOT_SETTINGS.API_CONFIG.attachToBot)
                        Main.API.setPosition((int) getBounds().getMaxX() - 6, (int) getBounds().getMinY());
                }
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> main.configManager.saveConfig()));
    }

    private void setComponentPosition() {
        mainPanel.setLayout(new MigLayout("ins 0, gap 0, wrap 1, fill", "[]", "[][][grow]"));
        mainPanel.add(titleBar = new MainTitleBar(main, this), "grow, span");
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

    public void tryClose() {
        if (main.config.BOT_SETTINGS.BOT_GUI.CONFIRM_EXIT) exitConfirmation.setVisible(true);
        else {
            System.out.println("Exit button pressed, exiting");
            System.exit(0);
        }
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

        mapDrawer.repaint();
    }

}
