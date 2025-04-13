package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.SizedLabel;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShipModeEditor extends JPanel implements OptionEditor<ShipMode> {

    private HeroAPI.Configuration config;

    private final List<ConfigButton> configButtons = Arrays.stream(HeroAPI.Configuration.values())
            .filter(c -> c != HeroAPI.Configuration.UNKNOWN)
            .map(ConfigButton::new).collect(Collectors.toList());

    private SelectableItem.Formation formation;

    private final JComboBox<SelectableItem.Formation> comboBox = new JComboBox<>(SelectableItem.Formation.values());

    public ShipModeEditor() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        for (ConfigButton configButton : this.configButtons) {
            this.add(configButton);
        }
        this.comboBox.addItemListener(item -> {
            if (item.getStateChange() == ItemEvent.SELECTED) {
                setFormation((SelectableItem.Formation) item.getItem());
            }
        });
        this.add(new SizedLabel("  "));
        this.add(this.comboBox);
    }


    public JComponent getEditorComponent(ConfigSetting<ShipMode> shipConfig) {
        ShipMode value = shipConfig.getValue();
        setConfig(value.getConfiguration());
        setFormation(value.getFormation());
        return this;
    }

    private void setConfig(HeroAPI.Configuration config) {
        this.config = config;
        configButtons.forEach(ConfigButton::repaint);
    }

    private void setFormation(SelectableItem.Formation formation) {
        this.formation = formation;
        this.comboBox.setSelectedItem(formation);
    }

    @Override
    public ShipMode getEditorValue() {
        return ShipMode.of(config, formation);
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
        private final HeroAPI.Configuration config;

        ConfigButton(HeroAPI.Configuration config) {
            super(String.valueOf(config.ordinal()));
            this.config = config;
            //noinspection SuspiciousNameCombination
            setPreferredSize(new Dimension(AdvancedConfig.EDITOR_HEIGHT, AdvancedConfig.EDITOR_HEIGHT));

            addActionListener(a -> setConfig(config));
        }

        @Override
        public boolean isDefaultButton() {
            return ShipModeEditor.this.config == config;
        }
    }
}
