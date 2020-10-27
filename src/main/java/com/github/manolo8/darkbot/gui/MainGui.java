package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.gui.components.ExitConfirmation;
import com.github.manolo8.darkbot.gui.titlebar.MainTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

        Config.BotSettings botSettings = ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS;
        Rectangle bounds = new Rectangle(botSettings.mainGuiX, botSettings.mainGuiY,
                botSettings.mainGuiWidth, botSettings.mainGuiHeight);

        // setting size
        if (!botSettings.SAVE_MAIN_GUI_SCREEN_SIZE || isBiggerThanScreenSize(bounds))
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        else
            setSize(botSettings.mainGuiWidth, botSettings.mainGuiHeight);

        // setting location
        if (!botSettings.SAVE_MAIN_GUI_POS ||
                (botSettings.mainGuiX == Integer.MIN_VALUE && botSettings.mainGuiY == Integer.MIN_VALUE) ||
                !canBeSeen(bounds))
            setLocationRelativeTo(null);
        else
            setLocation(botSettings.mainGuiX, botSettings.mainGuiY);

        setIconImage(ICON);

        setComponentPosition();

        WindowUtils.setupUndecorated(this, mainPanel);
        setVisible(true);

        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(main.config.BOT_SETTINGS.DISPLAY.ALWAYS_ON_TOP);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                if (botSettings.SAVE_MAIN_GUI_POS) {
                    botSettings.mainGuiX = getX();
                    botSettings.mainGuiY = getY();
                    ConfigEntity.changed();
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {
                if (botSettings.SAVE_MAIN_GUI_SCREEN_SIZE) {
                    botSettings.mainGuiWidth = getWidth();
                    botSettings.mainGuiHeight = getHeight();
                    ConfigEntity.changed();
                }
            }
        });
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

    private boolean isBiggerThanScreenSize(Rectangle bounds) {
        return forEachScreenDeviceIsFilterPresent(device -> {
            Rectangle deviceBounds = device.getDefaultConfiguration().getBounds();
            return bounds.height > deviceBounds.height || bounds.width > deviceBounds.width;
        });
    }

    private boolean canBeSeen(Rectangle bounds) {
        return forEachScreenDeviceIsFilterPresent(device -> device.getDefaultConfiguration().getBounds().intersects(bounds));
    }

    private static boolean forEachScreenDeviceIsFilterPresent(Predicate<GraphicsDevice> filter) {
        if (filter == null) return false;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return Arrays.stream(ge.getScreenDevices()).anyMatch(filter);
    }

}
