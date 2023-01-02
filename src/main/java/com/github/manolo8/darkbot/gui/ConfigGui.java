package com.github.manolo8.darkbot.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.TabbedPane;
import com.github.manolo8.darkbot.gui.players.PlayerEditor;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.gui.titlebar.ConfigPicker;
import com.github.manolo8.darkbot.gui.titlebar.ConfigTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import com.github.manolo8.darkbot.gui.zones.ZonesEditor;
import eu.darkbot.api.config.ConfigSetting;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ConfigGui extends JFrame {

    private final Main main;

    private TabbedPane tabbedPane;

    private AdvancedConfig advancedPane;
    private ZonesEditor zones;
    private PlayerEditor playerEditor;
    private ConfigPicker configPicker;
    private AbstractButton pluginTab;
    private PluginDisplay pluginDisplay;

    private final Lazy<Boolean> stateChange = new Lazy.NoCache<>();

    public ConfigGui(Main main) throws HeadlessException {
        super("DarkBot Configuration");
        this.main = main;

        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, false);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_TITLE, false);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        initComponents();
        setComponentPosition();
        setComponentData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ConfigEntity.changed();
                stateChange.send(false);
            }
        });

        Config.BotSettings.BotGui guiConfig = main.config.BOT_SETTINGS.BOT_GUI;
        WindowUtils.setWindowSize(this, guiConfig.SAVE_GUI_POS, guiConfig.CONFIG_GUI_WINDOW);
    }

    private void initComponents() {
        tabbedPane = new TabbedPane();

        advancedPane = new AdvancedConfig(main.pluginAPI);
        zones = new ZonesEditor();
        playerEditor = new PlayerEditor();
        configPicker = new ConfigPicker();
        pluginDisplay = new PluginDisplay();
    }

    private void setComponentPosition() {
        tabbedPane.addTab(null, "tabs.general", advancedPane);
        tabbedPane.addTab(null, "tabs.zones", zones);
        tabbedPane.addTab(null, "tabs.players", playerEditor);
        pluginTab = tabbedPane.addHiddenTab(UIUtils.getIcon("plugins"), "tabs.plugins", pluginDisplay);

        setJMenuBar(new ConfigTitleBar(this, tabbedPane.getHeader(), configPicker, pluginTab, main));
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setComponentData() {
        advancedPane.setEditingConfig(main.configHandler.getConfigRoot());
        advancedPane.rebuildUI();
        main.pluginHandler.addListener(advancedPane);
        zones.setup(main);
        configPicker.setup(main);
        playerEditor.setup(main);
        pluginDisplay.setup(main, pluginTab);
    }

    void setCustomConfig(@Nullable ConfigSetting.Parent<?> config) {
        SwingUtilities.invokeLater(() -> advancedPane.setCustomConfig(config));
    }

    void updateConfigTreeListeners() {
        SwingUtilities.invokeLater(() -> advancedPane.updateConfigTreeListeners());
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        stateChange.send(b);
    }

    void addVisibilityListener(Consumer<Boolean> event) {
        stateChange.add(event);
    }

}
