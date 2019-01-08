package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigForm {


    private JTabbedPane tabbedPane;
    public JPanel content;
    private JCheckBox stayAwayFromEnemiesCheckBox;
    private JCheckBox usePetAutoLooterCheckBox;
    private JRadioButton collectorSLC;
    private JSlider repairPercent;
    private JTable npcTable;
    private JTable boxTable;
    private JComboBox<String> workingMap;
    private JRadioButton lootSLC;
    private JCheckBox runFromEnemiesCheckBox;
    private JCheckBox autoCloackCheckBox;
    private JTextField cloackKey;
    private JTextField ammoKey;
    private JTextField sabKey;
    private JCheckBox autoSabCheck;
    private JComboBox<String> runConfig;
    private JComboBox<String> offensiveConfig;
    private JTextField runFormationKey;
    private JTextField offensiveFormationKey;
    private JRadioButton lootNCollectoSLC;

    private Config config;


    public ConfigForm(Main main) {

        this.config = main.config;

        for (String string : main.starManager.getAllMaps()) {
            workingMap.addItem(string);
        }

        runConfig.addItem("1");
        runConfig.addItem("2");
        offensiveConfig.addItem("1");
        offensiveConfig.addItem("2");

        stayAwayFromEnemiesCheckBox.setSelected(config.STAY_AWAY_FROM_ENEMIES);
        repairPercent.setValue((int) (config.REPAIR_HP * 100));

        Map map = main.starManager.fromId(config.WORKING_MAP);

        if (map != null) {
            workingMap.setSelectedItem(map.name);
        }

        runFromEnemiesCheckBox.setSelected(config.RUN_FROM_ENEMIES);
        autoCloackCheckBox.setSelected(config.AUTO_CLOACK);
        autoSabCheck.setSelected(config.AUTO_SAB);
        cloackKey.setText(String.valueOf(config.AUTO_CLOACK_KEY));
        ammoKey.setText(String.valueOf(config.AMMO_KEY));
        sabKey.setText(String.valueOf(config.AUTO_SAB_KEY));
        runFormationKey.setText(String.valueOf(config.RUN_FORMATION));
        offensiveFormationKey.setText(String.valueOf(config.OFFENSIVE_FORMATION));
        runConfig.setSelectedItem(String.valueOf(config.RUN_CONFIG));
        offensiveConfig.setSelectedItem(String.valueOf(config.OFFENSIVE_CONFIG));
        collectorSLC.setSelected(config.CURRENT_MODULE == 0);
        lootSLC.setSelected(config.CURRENT_MODULE == 1);
        lootNCollectoSLC.setSelected(config.CURRENT_MODULE == 2);

        stayAwayFromEnemiesCheckBox.addChangeListener(e -> config.STAY_AWAY_FROM_ENEMIES = stayAwayFromEnemiesCheckBox.isSelected());

        repairPercent.addChangeListener(e -> config.REPAIR_HP = ((double) repairPercent.getValue() / repairPercent.getMaximum()));

        workingMap.addItemListener(e -> config.WORKING_MAP = main.starManager.fromName((String) e.getItem()).id);

        runFromEnemiesCheckBox.addChangeListener(e -> config.RUN_FROM_ENEMIES = runFromEnemiesCheckBox.isSelected());

        autoCloackCheckBox.addChangeListener(e -> config.AUTO_CLOACK = autoCloackCheckBox.isSelected());

        autoSabCheck.addChangeListener(e -> config.AUTO_SAB = autoSabCheck.isSelected());

        runConfig.addItemListener(e -> config.RUN_CONFIG = Integer.parseInt((String) e.getItem()));

        offensiveConfig.addItemListener(e -> config.OFFENSIVE_CONFIG = Integer.parseInt((String) e.getItem()));

        offensiveFormationKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                offensiveFormationKey.setText(String.valueOf(e.getKeyChar()));
                config.OFFENSIVE_FORMATION = e.getKeyChar();
            }
        });

        runFormationKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                runFormationKey.setText(String.valueOf(e.getKeyChar()));
                config.RUN_FORMATION = e.getKeyChar();
            }
        });

        sabKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                sabKey.setText(String.valueOf(e.getKeyChar()));
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

        cloackKey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cloackKey.setText(String.valueOf(e.getKeyChar()));
                config.AUTO_CLOACK_KEY = e.getKeyChar();
            }
        });

        collectorSLC.addChangeListener(e -> {
            lootNCollectoSLC.setSelected(false);
            lootSLC.setSelected(false);

            config.CURRENT_MODULE = 0;
            main.setModule(new CollectorModule());
        });

        lootSLC.addChangeListener(e -> {
            lootNCollectoSLC.setSelected(false);
            collectorSLC.setSelected(false);

            config.CURRENT_MODULE = 1;
            main.setModule(new LootModule());
        });

        lootNCollectoSLC.addChangeListener(e -> {
            lootSLC.setSelected(false);
            collectorSLC.setSelected(false);

            config.CURRENT_MODULE = 2;
            main.setModule(new LootNCollectorModule());
        });

        ButtonGroup group = new ButtonGroup();
        group.add(lootSLC);
        group.add(lootNCollectoSLC);
        group.add(collectorSLC);
    }


    private void createUIComponents() {
        createNpcTable();
        createBoxTable();
    }

    private void createNpcTable() {
        Set<Entry<String, NpcInfo>> types = config.npcInfos.entrySet();

        String[] header = new String[]{"Name", "Radius", "Priority", "Kill", "Last to kill"};
        Object[][] content = new Object[types.size()][5];

        int index = 0;

        for (Entry<String, NpcInfo> entry : types) {

            String name = entry.getKey();
            NpcInfo info = entry.getValue();

            content[index][0] = name;
            content[index][1] = info.radius;
            content[index][2] = info.priority;
            content[index][3] = info.kill;
            content[index][4] = info.killOnlyIfIsLast;

            index++;
        }

        npcTable = new JTable(content, header) {
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

                NpcInfo info = config.npcInfos.get(name);

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
        };

        npcTable.getColumnModel().getColumn(0).setPreferredWidth(300);
    }

    private void createBoxTable() {
        Set<Entry<String, BoxInfo>> types = config.boxInfos.entrySet();

        String[] header = new String[]{"Name", "Collect", "Wait"};
        Object[][] content = new Object[types.size()][3];

        int index = 0;

        for (Entry<String, BoxInfo> entry : types) {

            String name = entry.getKey();
            BoxInfo info = entry.getValue();

            content[index][0] = name;
            content[index][1] = info.collect;
            content[index][2] = info.waitTime;

            index++;
        }

        boxTable = new JTable(content, header) {
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

                BoxInfo info = config.boxInfos.get(name);

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
        };

        boxTable.getColumnModel().getColumn(0).setPreferredWidth(300);
    }
}
