package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.components.TabbedPane;
import com.github.manolo8.darkbot.gui.players.PlayerEditor;
import com.github.manolo8.darkbot.gui.plugins.PluginDisplay;
import com.github.manolo8.darkbot.gui.safety.SafetiesEditor;
import com.github.manolo8.darkbot.gui.titlebar.ConfigTitleBar;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.gui.utils.window.WindowUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ConfigGui extends JFrame {

    private final Main main;
    private final Config config;

    private final JPanel mainPanel = new JPanel();

    private TabbedPane tabbedPane;

    private AdvancedConfig advancedPane;
    private ZoneEditor preferredZones;
    private ZoneEditor avoidedZones;
    private SafetiesEditor safeEditor;
    private PlayerEditor playerEditor;
    private MainButton pluginTab;
    private PluginDisplay pluginDisplay;

    private Lazy<Boolean> stateChange = new Lazy.NoCache<>();

    public ConfigGui(Main main) throws HeadlessException {
        super("DarkBot - Config");

        this.main = main;
        this.config = main.config;

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
    }

    private void initComponents() {
        tabbedPane = new TabbedPane();

        advancedPane = new AdvancedConfig();
        preferredZones = new ZoneEditor();
        avoidedZones = new ZoneEditor();
        safeEditor = new SafetiesEditor();
        playerEditor = new PlayerEditor();
        pluginDisplay = new PluginDisplay();
    }

    private void setComponentPosition() {
        tabbedPane.addTab(null, "tabs.general", advancedPane);
        tabbedPane.addTab(null, "tabs.preferred_zones", preferredZones);
        tabbedPane.addTab(null, "tabs.avoided_zones", avoidedZones);
        tabbedPane.addTab(null, "tabs.safety_places", safeEditor);
        tabbedPane.addTab(null, "tabs.players", playerEditor);
        pluginTab = tabbedPane.addHiddenTab(UIUtils.getIcon("plugins"), "tabs.plugins", pluginDisplay);

        mainPanel.setLayout(new MigLayout("ins 0, gap 0, wrap 1, fill", "[]", "[][grow]"));
        mainPanel.add(new ConfigTitleBar(this, tabbedPane.getHeader(), pluginTab, main), "grow, span");

        mainPanel.add(tabbedPane, "grow, span");
    }

    private void setComponentData() {
        advancedPane.setEditingConfig(config);
        main.pluginHandler.addListener(advancedPane);
        preferredZones.setup(main, config.PREFERRED);
        avoidedZones.setup(main, config.AVOIDED);
        safeEditor.setup(main);
        playerEditor.setup(main);
        pluginDisplay.setup(main, pluginTab);
    }

    void setCustomConfig(String name, Object config) {
        advancedPane.setCustomConfig(name, config);
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
