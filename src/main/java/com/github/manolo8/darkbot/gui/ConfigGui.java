package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.EventModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConfigGui extends JFrame {

    private final Main main;
    private final Config config;

    private JTabbedPane tabbedPane;

    private JPanel generalPane;
    private JPanel ggPane;
    private AdvancedConfig advancedPane;
    private ZoneEditor preferredZones;
    private ZoneEditor avoidedZones;

    //GENERAL
    private JComboBox<String> workingMap;

    private JComboBox<String> reviveMethod;

    private JComboBox<Integer> runConfig;
    private JComboBox<Integer> offensiveConfig;

    private JTextField runFormation;
    private JTextField offensiveFormation;

    private JTextField refreshTime;
    private JTextField maxDeaths;

    private JSlider repairHp;
    private JSlider waitHp;

    private JRadioButton moduleCollector;
    private JRadioButton moduleLoot;
    private JRadioButton moduleLootNCollector;
    private JRadioButton moduleEvent;
    //GENERAL

    public ConfigGui(Main main) throws HeadlessException {
        super("DarkBot - Config");

        this.main = main;
        this.config = main.config;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setAlwaysOnTop(main.config.MISCELLANEOUS.DISPLAY.ALWAYS_ON_TOP);

        initComponents();
        setComponentPosition();
        setComponentData();
        setComponentEvent();

        validate();
        repaint();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.config.changed = true;
            }
        });
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        generalPane = new JPanel();
        ggPane = new JPanel();
        advancedPane = new AdvancedConfig();
        preferredZones = new ZoneEditor();
        avoidedZones = new ZoneEditor();


        //GENERAL
        workingMap = new JComboBox<>();

        reviveMethod = new JComboBox<>();

        runConfig = new JComboBox<>();
        offensiveConfig = new JComboBox<>();

        runFormation = new JTextField();
        offensiveFormation = new JTextField();

        refreshTime = new JTextField();
        maxDeaths = new JTextField();

        moduleCollector = new JRadioButton("Collector");
        moduleLoot = new JRadioButton("Loot");
        moduleLootNCollector = new JRadioButton("Loot and collector");
        moduleEvent = new JRadioButton("Event");

        repairHp = new JSlider();
        waitHp = new JSlider();

        ButtonGroup group = new ButtonGroup();
        group.add(moduleCollector);
        group.add(moduleLoot);
        group.add(moduleLootNCollector);
        group.add(moduleEvent);

        //GENERAL
    }

    private void setComponentPosition() {

        tabbedPane.addTab("General", generalPane);
        tabbedPane.addTab("GG", ggPane);
        tabbedPane.addTab("Advanced", advancedPane);
        tabbedPane.addTab("Preferred Zones", preferredZones);
        tabbedPane.addTab("Avoided Zones", avoidedZones);

        add(tabbedPane);

        //GENERAL

        generalPane.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        Insets normal = new Insets(0, 5, 5, 5);
        Insets sep = new Insets(15, 5, 15, 5);

        c.fill = GridBagConstraints.BOTH;

        c.insets = normal;

        int y = 0;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Working map"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(workingMap, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        c.insets = sep;
        generalPane.add(separator(), c);
        c.insets = normal;

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Running config"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 1;
        generalPane.add(runConfig, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 2;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Running formation"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        generalPane.add(runFormation, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Offensive config"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 1;
        generalPane.add(offensiveConfig, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 2;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Offensive formation"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        generalPane.add(offensiveFormation, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        c.insets = sep;
        generalPane.add(separator(), c);
        c.insets = normal;

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Refresh"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(refreshTime, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Revive"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(reviveMethod, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Deaths"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(maxDeaths, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Repair Start %"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(repairHp, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Repair Stop %"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        generalPane.add(waitHp, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        c.insets = sep;
        generalPane.add(separator(), c);
        c.insets = normal;

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        generalPane.add(new JLabel("Module"), c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 1;
        generalPane.add(moduleCollector, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 2;
        c.gridwidth = 1;
        generalPane.add(moduleLoot, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        generalPane.add(moduleLootNCollector, c);
        c.weightx = 0;
        c.gridy = ++y;
        c.gridx = 1;
        c.gridwidth = 1;
        generalPane.add(moduleEvent, c);
        //GENERAL

        // GG
        ggPane.setLayout(new BoxLayout(ggPane, BoxLayout.Y_AXIS));
    }

    private void setComponentData() {

        for (String string : main.starManager.getAccessibleMaps())
            workingMap.addItem(string);

        runConfig.addItem(1);
        runConfig.addItem(2);
        offensiveConfig.addItem(1);
        offensiveConfig.addItem(2);

        reviveMethod.addItem("Base");
        reviveMethod.addItem("Portal");
        reviveMethod.addItem("Local");

        reviveMethod.setSelectedIndex(Math.min(Math.max((int) config.GENERAL.SAFETY.REVIVE_LOCATION - 1, 2), 0));

        repairHp.setValue((int) (config.GENERAL.SAFETY.REPAIR_HP * 100));
        waitHp.setValue((int) (config.GENERAL.SAFETY.REPAIR_TO_HP * 100));

        Map map = main.starManager.byId(config.WORKING_MAP);

        if (map != null)
            workingMap.setSelectedItem(map.name);

        refreshTime.setText(String.valueOf(config.MISCELLANEOUS.REFRESH_TIME));
        maxDeaths.setText(String.valueOf(config.GENERAL.SAFETY.MAX_DEATHS));
        runFormation.setText(String.valueOf(config.GENERAL.RUN.FORMATION));
        offensiveFormation.setText(String.valueOf(config.GENERAL.OFFENSIVE.FORMATION));
        runConfig.setSelectedItem(config.GENERAL.RUN.CONFIG);
        offensiveConfig.setSelectedItem(config.GENERAL.OFFENSIVE.CONFIG);
        moduleCollector.setSelected(config.CURRENT_MODULE == 0);
        moduleLoot.setSelected(config.CURRENT_MODULE == 1);
        moduleLootNCollector.setSelected(config.CURRENT_MODULE == 2);
        moduleEvent.setSelected(config.CURRENT_MODULE == 3);

        for (String string : main.starManager.getGGMaps())
            ggPane.add(new JCheckBox(string));

        advancedPane.setEditingConfig(config);
        preferredZones.setup(main, config.PREFERRED);
        avoidedZones.setup(main, config.AVOIDED);
    }

    private JSeparator separator() {
        JSeparator separator = new JSeparator();

        separator.setForeground(Color.gray);

        return separator;
    }

    private void setComponentEvent() {

        //GENERAL

        repairHp.addChangeListener(e -> config.GENERAL.SAFETY.REPAIR_HP = ((double) repairHp.getValue() / repairHp.getMaximum()));

        waitHp.addChangeListener(e -> config.GENERAL.SAFETY.REPAIR_TO_HP = ((double) waitHp.getValue() / waitHp.getMaximum()));

        workingMap.addItemListener(e -> config.WORKING_MAP = main.starManager.byName((String) e.getItem()).id);

        reviveMethod.addItemListener(e -> config.GENERAL.SAFETY.REVIVE_LOCATION = reviveMethod.getSelectedIndex() + 1);

        runConfig.addItemListener(e -> config.GENERAL.RUN.CONFIG = (int) e.getItem());

        offensiveConfig.addItemListener(e -> config.GENERAL.OFFENSIVE.CONFIG = (int) e.getItem());

        refreshTime.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    int value = Integer.parseInt(refreshTime.getText());

                    if (value >= 0) {
                        config.MISCELLANEOUS.REFRESH_TIME = value;
                    } else {
                        config.MISCELLANEOUS.REFRESH_TIME = 0;
                    }

                } catch (NumberFormatException ignored) {
                    config.MISCELLANEOUS.REFRESH_TIME = 0;
                    refreshTime.setText("0");
                }

                config.changed = true;
            }
        });

        maxDeaths.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {

                    config.GENERAL.SAFETY.MAX_DEATHS = Integer.parseInt(maxDeaths.getText());

                } catch (NumberFormatException ignored) {
                    config.GENERAL.SAFETY.MAX_DEATHS = 10;
                    maxDeaths.setText("10");
                }

                config.changed = true;
            }
        });

        offensiveFormation.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                offensiveFormation.setText(String.valueOf(e.getKeyChar()));
                config.GENERAL.OFFENSIVE.FORMATION = e.getKeyChar();
            }
        });

        runFormation.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                runFormation.setText(String.valueOf(e.getKeyChar()));
                config.GENERAL.RUN.FORMATION = e.getKeyChar();
            }
        });


        moduleCollector.addItemListener(e -> {

            if (e.getStateChange() != ItemEvent.SELECTED) return;

            config.CURRENT_MODULE = 0;
            main.setModule(new CollectorModule());
        });

        moduleLoot.addItemListener(e -> {

            if (e.getStateChange() != ItemEvent.SELECTED) return;

            config.CURRENT_MODULE = 1;
            main.setModule(new LootModule());
        });

        moduleLootNCollector.addItemListener(e -> {

            if (e.getStateChange() != ItemEvent.SELECTED) return;

            config.CURRENT_MODULE = 2;
            main.setModule(new LootNCollectorModule());
        });

        moduleEvent.addItemListener(e -> {

            if (e.getStateChange() != ItemEvent.SELECTED) return;

            config.CURRENT_MODULE = 3;
            main.setModule(new EventModule());
        });

        //GENERAL
    }

}
