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
    private JPanel collectPane;
    private JPanel lootPane;
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

    //COLLECT
    private JCheckBox stayAwayFromEnemies;

    private JCheckBox autoClock;
    private JTextField autoCloackKey;

    private JTable boxTable;
    //COLLECT

    //LOOT
    private JCheckBox runFromEnemies;
    private JCheckBox runFromEnemiesInSight;

    private JTextField ammoKey;

    private JCheckBox autoSab;
    private JTextField ammoSabKey;

    private JTextField npcTableNameFilter;
    private JComboBox<String> npcTableMapFilter;
    private JTable npcTable;
    //LOOT

    public ConfigGui(Main main) throws HeadlessException {
        super("DarkBot - Config");

        this.main = main;
        this.config = main.config;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);
        setAlwaysOnTop(main.config.MISCELLANEOUS.ALWAYS_ON_TOP);

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
        collectPane = new JPanel();
        lootPane = new JPanel();
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

        //COLLECT
        stayAwayFromEnemies = new JCheckBox("Stay away from enemies");

        autoClock = new JCheckBox("Auto cloack");
        autoCloackKey = new JTextField();

        boxTable = new JTable(new BoxTableModel());
        //COLLECT

        //LOOT
        runFromEnemies = new JCheckBox("Run from enemies");
        runFromEnemiesInSight = new JCheckBox("Run from enemies in sight");

        ammoKey = new JTextField();

        autoSab = new JCheckBox("Auto SAB");
        ammoSabKey = new JTextField();

        npcTableMapFilter = new JComboBox<>();
        npcTableNameFilter = new JTextField();
        npcTable = new JTable(new NpcTableModel());
        //LOOT
    }

    private void setComponentPosition() {

        tabbedPane.addTab("General", generalPane);
        tabbedPane.addTab("Collect", collectPane);
        tabbedPane.addTab("Loot", lootPane);
        tabbedPane.addTab("GG", ggPane);
        tabbedPane.addTab("Advanced", advancedPane);
        tabbedPane.addTab("Preferred Zones", preferredZones);
        tabbedPane.addTab("Avoided Zones (not working yet)", avoidedZones);

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

        //COLLECT
        collectPane.setLayout(new GridBagLayout());

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;

        c.ipady = 0;
        c.ipadx = 0;
        c.insets = normal;

        y = 0;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        collectPane.add(stayAwayFromEnemies, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 2;
        collectPane.add(autoClock, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 2;
        c.gridwidth = 1;
        collectPane.add(new JLabel("Key"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        collectPane.add(autoCloackKey, c);

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        c.insets = sep;
        collectPane.add(separator(), c);
        c.insets = normal;

        ++y;

        c.weightx = 1;
        c.weighty = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;
        collectPane.add(new JScrollPane(boxTable), c);

        //COLLECT

        //LOOT
        lootPane.setLayout(new GridBagLayout());

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;

        c.ipady = 0;
        c.ipadx = 0;
        c.insets = normal;

        y = 0;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 2;
        lootPane.add(runFromEnemies, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 2;
        lootPane.add(runFromEnemiesInSight, c);

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        lootPane.add(new JLabel("Normal Ammo"), c);
        c.gridx = 2;
        lootPane.add(new JLabel("KEY"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        lootPane.add(ammoKey, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 2;
        lootPane.add(autoSab, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 2;
        c.gridwidth = 1;
        lootPane.add(new JLabel("KEY"), c);
        c.weightx = 1;
        c.gridy = y;
        c.gridx = 3;
        c.gridwidth = 1;
        lootPane.add(ammoSabKey, c);

        ++y;

        c.weightx = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;

        c.insets = sep;
        lootPane.add(separator(), c);
        c.insets = normal;

        ++y;

        c.weightx = 0;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 1;
        lootPane.add(npcTableMapFilter, c);
        c.weightx = 0;
        c.gridy = y;
        c.gridx = 1;
        c.gridwidth = 3;
        lootPane.add(npcTableNameFilter, c);

        ++y;

        c.weightx = 1;
        c.weighty = 1;
        c.gridy = y;
        c.gridx = 0;
        c.gridwidth = 4;
        lootPane.add(new JScrollPane(npcTable), c);
        //LOOT

    }

    private void setComponentData() {

        for (String string : main.starManager.getAllMaps())
            workingMap.addItem(string);

        runConfig.addItem(1);
        runConfig.addItem(2);
        offensiveConfig.addItem(1);
        offensiveConfig.addItem(2);

        reviveMethod.addItem("Base");
        reviveMethod.addItem("Portal");
        reviveMethod.addItem("Local");

        reviveMethod.setSelectedIndex(Math.min(Math.max((int) config.GENERAL.SAFETY.REVIVE_LOCATION - 1, 2), 0));

        stayAwayFromEnemies.setSelected(config.STAY_AWAY_FROM_ENEMIES);
        repairHp.setValue((int) (config.GENERAL.SAFETY.REPAIR_HP * 100));
        waitHp.setValue((int) (config.GENERAL.SAFETY.REPAIR_TO_HP * 100));

        Map map = main.starManager.fromId(config.WORKING_MAP);

        if (map != null)
            workingMap.setSelectedItem(map.name);

        runFromEnemies.setSelected(config.LOOT.SAFETY.RUN_FROM_ENEMIES);
        runFromEnemiesInSight.setSelected(config.LOOT.SAFETY.RUN_FROM_ENEMIES_SIGHT);
        autoClock.setSelected(config.AUTO_CLOACK);
        refreshTime.setText(String.valueOf(config.MISCELLANEOUS.REFRESH_TIME));
        maxDeaths.setText(String.valueOf(config.GENERAL.SAFETY.MAX_DEATHS));
        autoSab.setSelected(config.AUTO_SAB);
        autoCloackKey.setText(String.valueOf(config.AUTO_CLOACK_KEY));
        ammoKey.setText(String.valueOf(config.AMMO_KEY));
        ammoSabKey.setText(String.valueOf(config.AUTO_SAB_KEY));
        runFormation.setText(String.valueOf(config.GENERAL.RUN.FORMATION));
        offensiveFormation.setText(String.valueOf(config.GENERAL.OFFENSIVE.FORMATION));
        runConfig.setSelectedItem(config.GENERAL.RUN.CONFIG);
        offensiveConfig.setSelectedItem(config.GENERAL.OFFENSIVE.CONFIG);
        moduleCollector.setSelected(config.CURRENT_MODULE == 0);
        moduleLoot.setSelected(config.CURRENT_MODULE == 1);
        moduleLootNCollector.setSelected(config.CURRENT_MODULE == 2);
        moduleEvent.setSelected(config.CURRENT_MODULE == 3);

        npcTableMapFilter.addItem("ALL");

        NpcTableModel npcModel = (NpcTableModel) npcTable.getModel();

        for (java.util.Map.Entry<String, NpcInfo> entry : config.LOOT.NPC_INFOS.entrySet()) {

            String name = entry.getKey();
            NpcInfo info = entry.getValue();

            npcModel.addEntry(name, info);
        }

        BoxTableModel boxModel = (BoxTableModel) boxTable.getModel();

        for (java.util.Map.Entry<String, BoxInfo> entry : config.COLLECT.BOX_INFOS.entrySet()) {

            String name = entry.getKey();
            BoxInfo info = entry.getValue();

            boxModel.addEntry(name, info);
        }


        boxTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        npcTable.getColumnModel().getColumn(0).setPreferredWidth(200);

        config.addedNpc.add(value -> npcModel.addEntry(value, config.LOOT.NPC_INFOS.get(value)));
        config.addedBox.add(value -> boxModel.addEntry(value, config.COLLECT.BOX_INFOS.get(value)));

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

        workingMap.addItemListener(e -> config.WORKING_MAP = main.starManager.fromName((String) e.getItem()).id);

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

        //COLLECT

        stayAwayFromEnemies.addChangeListener(e -> config.STAY_AWAY_FROM_ENEMIES = stayAwayFromEnemies.isSelected());

        autoClock.addItemListener(e -> config.AUTO_CLOACK = autoClock.isSelected());

        autoCloackKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                autoCloackKey.setText(String.valueOf(e.getKeyChar()));
                config.AUTO_CLOACK_KEY = e.getKeyChar();
            }
        });

        //COLLECT

        //LOOT

        runFromEnemies.addItemListener(e -> config.LOOT.SAFETY.RUN_FROM_ENEMIES = runFromEnemies.isSelected());

        runFromEnemiesInSight.addItemListener(e -> config.LOOT.SAFETY.RUN_FROM_ENEMIES_SIGHT = runFromEnemiesInSight.isSelected());

        autoSab.addItemListener(e -> config.AUTO_SAB = autoSab.isSelected());

        ammoSabKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                ammoSabKey.setText(String.valueOf(e.getKeyChar()));
                config.AUTO_SAB_KEY = e.getKeyChar();
            }
        });

        ammoKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                ammoKey.setText(String.valueOf(e.getKeyChar()));
                config.AMMO_KEY = e.getKeyChar();
            }
        });

        npcTableNameFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                NpcTableModel npcModel = (NpcTableModel) npcTable.getModel();

                npcModel.filterByName(npcTableNameFilter.getText());
            }
        });

        //LOOT
    }

    private class BoxTableModel extends DefaultTableModel {

        public BoxTableModel() {
            super(new Object[]{"Name", "Collect", "Wait"}, 0);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 1 ? Boolean.class : column == 2 ? Integer.class : super.getColumnClass(column);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);

            String name = (String) getValueAt(row, 0);

            BoxInfo info = config.COLLECT.BOX_INFOS.get(name);

            switch (column) {
                case 1:
                    info.collect = (boolean) value;
                    break;
                case 2:
                    info.waitTime = (int) value;
                    break;
            }

            config.changed = true;
        }

        public void addEntry(String name, BoxInfo info) {

            boolean collect = info.collect;
            int wait = info.waitTime;

            addRow(new Object[]{name, collect, wait});
        }
    }

    private class NpcTableModel extends DefaultTableModel {

        public NpcTableModel() {
            super(new Object[]{"Name", "Radius", "Priority", "Kill", "Last to kill"}, 0);
        }

        public void addEntry(String name, NpcInfo info) {

            double radius = info.radius;
            int priority = info.priority;
            boolean kill = info.kill;
            boolean last = info.killOnlyIfIsLast;

            addRow(new Object[]{name, radius, priority, kill, last});
        }

        public void filterByName(String filter) {
            setNumRows(0);

            filter = filter.toLowerCase();

            for (java.util.Map.Entry<String, NpcInfo> entry : config.LOOT.NPC_INFOS.entrySet()) {

                String name = entry.getKey();

                if (name.toLowerCase().contains(filter)) {
                    addEntry(name, entry.getValue());
                }

            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 3 || column == 4 ? Boolean.class : column == 1 ? Double.class : column == 2 ? Integer.class : super.getColumnClass(column);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            super.setValueAt(value, row, column);

            String name = (String) getValueAt(row, 0);

            NpcInfo info = config.LOOT.NPC_INFOS.get(name);

            switch (column) {
                case 1:
                    info.radius = (double) value;
                    break;
                case 2:
                    info.priority = (int) value;
                    break;
                case 3:
                    info.kill = (boolean) value;
                    break;
                case 4:
                    info.killOnlyIfIsLast = (boolean) value;
                    break;
            }

            config.changed = true;

        }
    }
}
