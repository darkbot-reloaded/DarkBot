package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.utils.Lazy;
import com.github.manolo8.darkbot.gui.components.TabbedPane;
import com.github.manolo8.darkbot.gui.safety.SafetiesEditor;
import com.github.manolo8.darkbot.gui.titlebar.ConfigTitleBar;
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
    private JPanel ggPane;

    private Lazy<Boolean> stateChange = new Lazy<>();

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
        ggPane = new JPanel();
    }

    private void setComponentPosition() {
        mainPanel.setLayout(new MigLayout("ins 0, gap 0, wrap 1, fill", "[]", "[][grow]"));
        mainPanel.add(new ConfigTitleBar(this, tabbedPane.getHeader(), main), "grow, span");

        tabbedPane.addTab("General", advancedPane);
        tabbedPane.addTab("Preferred Zones", preferredZones);
        tabbedPane.addTab("Avoided Zones", avoidedZones);
        tabbedPane.addTab("Safety places", safeEditor);
        //tabbedPane.addTab("GG", ggPane);

        mainPanel.add(tabbedPane, "grow, span");

        // GG
        ggPane.setLayout(new BoxLayout(ggPane, BoxLayout.Y_AXIS));
    }

    private void setComponentData() {
        for (String string : main.starManager.getGGMaps())
            ggPane.add(new JCheckBox(string));

        advancedPane.setEditingConfig(config);
        preferredZones.setup(main, config.PREFERRED);
        avoidedZones.setup(main, config.AVOIDED);
        safeEditor.setup(main);
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
