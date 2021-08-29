package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.components.TabbedPane;
import com.github.manolo8.darkbot.gui.players.PlayerEditor;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.gui.titlebar.ConfigPicker;
import com.github.manolo8.darkbot.gui.zones.ZonesEditor;
import com.github.manolo8.darkbot.gui.titlebar.ConfigTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import eu.darkbot.api.config.ConfigSetting;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ConfigGui extends JFrame {

    private final Main main;

    private final JPanel mainPanel = new JPanel();

    private TabbedPane tabbedPane;

    private AdvancedConfig advancedPane;
    private ZonesEditor zones;
    private PlayerEditor playerEditor;
    private ConfigPicker configPicker;
    private MainButton pluginTab;
    private PluginDisplay pluginDisplay;

    private final Lazy<Boolean> stateChange = new Lazy.NoCache<>();

    public ConfigGui(Main main) throws HeadlessException {
        super("DarkBot Configuration");

        this.main = main;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        initComponents();
        setComponentPosition();
        setComponentData();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.config.changed = true;
                stateChange.send(false);
            }
        });

        WindowUtils.setupUndecorated(this, mainPanel);

        Config.BotSettings.BotGui guiConfig = main.config.BOT_SETTINGS.BOT_GUI;
        WindowUtils.setWindowSize(this, guiConfig.SAVE_GUI_POS, guiConfig.CONFIG_GUI_WINDOW);
    }

    private void initComponents() {
        tabbedPane = new TabbedPane();

        advancedPane = new AdvancedConfig();
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

        mainPanel.setLayout(new MigLayout("ins 0, gap 0, wrap 1, fill", "[]", "[][grow]"));
        mainPanel.add(new ConfigTitleBar(this, tabbedPane.getHeader(), configPicker, pluginTab, main), "grow, span");

        mainPanel.add(tabbedPane, "grow, span");
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

    void setCustomConfig(ConfigSetting.Parent<?>... configs) {
        SwingUtilities.invokeLater(() -> advancedPane.setCustomConfig(configs));
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
