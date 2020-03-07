package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class JShipConfigField extends JPanel implements OptionEditor {

    private JButton config1 = new ConfigButton(1), config2 = new ConfigButton(2);
    private FormationField formation = new FormationField();

    private Config.ShipConfig editing;

    public JShipConfigField() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        add(config1);
        add(config2);
        add(new javax.swing.JLabel("      Formation  "));
        add(formation);
    }

    private void setConfig(int num) {
        config1.setBackground(num == 1 ? UIManager.getColor("Tree.selectionBackground") : UIManager.getColor("Button.background"));
        config2.setBackground(num == 2 ? UIManager.getColor("Tree.selectionBackground") : UIManager.getColor("Button.background"));
        if (editing != null) {
            this.editing.CONFIG = num;
            ConfigEntity.changed();
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.editing = null;
        Config.ShipConfig conf = field.get();
        setConfig(conf.CONFIG);
        formation.setText(Objects.toString(conf.FORMATION, ""));
        formation.requestFocus();

        this.editing = conf;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    private class ConfigButton extends JButton {
        ConfigButton(int config) {
            super(config + "");
            putClientProperty("JButton.buttonType", "square");
            //noinspection SuspiciousNameCombination
            setPreferredSize(new Dimension(AdvancedConfig.EDITOR_HEIGHT, AdvancedConfig.EDITOR_HEIGHT));
            setFocusable(false);

            addActionListener(a -> setConfig(config));
        }
    }

    private class FormationField extends JCharField {
        @Override
        protected void setValue(Character value) {
            if (editing == null) return;
            editing.FORMATION = value;
            ConfigEntity.changed();
        }
    }

}
