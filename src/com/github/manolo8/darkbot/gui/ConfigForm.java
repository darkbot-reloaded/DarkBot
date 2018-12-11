package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.objects.NpcType;
import com.github.manolo8.darkbot.modules.CollectorModule;
import com.github.manolo8.darkbot.modules.LootModule;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

public class ConfigForm {


    private JTabbedPane tabbedPane;
    public JPanel content;
    private JCheckBox stayAwayFromEnemiesCheckBox;
    private JCheckBox usePetAutoLooterCheckBox;
    private JRadioButton collectorSLC;
    private JSlider repairPercent;
    private JTable npcTable;
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
    private JPanel lootPanel;

    private Main main;
    private Config config;


    public ConfigForm(Main main) {

        this.main = main;
        this.config = main.config;

        npcTable.setTableHeader(new JTableHeader());

        stayAwayFromEnemiesCheckBox.addChangeListener(e -> config.STAY_AWAY_FROM_ENEMIES = stayAwayFromEnemiesCheckBox.isSelected());

        repairPercent.addChangeListener(e -> config.REPAIR_HP = ((double) repairPercent.getValue() / repairPercent.getMaximum()));

        for (String string : main.starManager.starSystemName.keySet()) {
            workingMap.addItem(string);
        }

        workingMap.addItemListener(e -> config.WORKING_MAP = main.starManager.starSystemName.get(e.getItem()).id);

        runFromEnemiesCheckBox.addChangeListener(e -> config.RUN_FROM_ENEMIES = runFromEnemiesCheckBox.isSelected());

        autoCloackCheckBox.addChangeListener(e -> config.AUTO_CLOACK = autoCloackCheckBox.isSelected());

        autoSabCheck.addChangeListener(e -> config.AUTO_SAB = autoSabCheck.isSelected());

        runConfig.addItem("1");
        runConfig.addItem("2");
        offensiveConfig.addItem("1");
        offensiveConfig.addItem("2");

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
            if (collectorSLC.isSelected()) {
                lootSLC.setSelected(false);
                config.CURRENT_MODULE = 0;
                main.setModule(new CollectorModule(main));
            } else {
                collectorSLC.setSelected(!lootSLC.isSelected());
            }
        });

        lootSLC.addChangeListener(e -> {
            if (lootSLC.isSelected()) {
                collectorSLC.setSelected(false);
                config.CURRENT_MODULE = 1;
                main.setModule(new LootModule(main));
            } else {
                lootSLC.setSelected(!collectorSLC.isSelected());
            }
        });
    }

    private void createUIComponents() {

        Collection<NpcType> types = Npc.npcType.values();

        String[] header = new String[]{"Name", "Radius"};
        Object[][] content = new Object[types.size()][2];

        int index = 0;

        for (NpcType type : types) {
            content[index][0] = type.name;
            content[index++][1] = type.radius;
        }

        npcTable = new JTable(content, header);
        npcTable.setEnabled(false);
    }
}
