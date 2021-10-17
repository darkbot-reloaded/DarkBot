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

    private final ConfigButton config1 = new ConfigButton(1), config2 = new ConfigButton(2);
    private final FormationField formation = new FormationField();

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
        config1.setSelected(num == 1);
        config2.setSelected(num == 2);
        if (editing == null) return;
        this.editing.CONFIG = num;
        ConfigEntity.changed();
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
        formation.setValue(conf.FORMATION);

        this.editing = conf;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    @Override
    public Dimension getReservedSize() {
        return new Dimension(250, 0);
    }

    private class ConfigButton extends JButton {
        public boolean selected;

        ConfigButton(int config) {
            super(String.valueOf(config));
            //noinspection SuspiciousNameCombination
            setPreferredSize(new Dimension(AdvancedConfig.EDITOR_HEIGHT, AdvancedConfig.EDITOR_HEIGHT));
            setFocusable(false);

            addActionListener(a -> setConfig(config));
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        public boolean isDefaultButton() {
            return selected;
        }
    }

    private class FormationField extends JCharField.ExtraBorder {
        @Override
        public void setValue(Character value) {
            setText(getDisplay(value));
            if (editing == null) return;
            editing.FORMATION = value;
            ConfigEntity.changed();
        }
    }

}
