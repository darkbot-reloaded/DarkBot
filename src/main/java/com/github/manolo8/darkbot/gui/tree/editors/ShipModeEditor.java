package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.managers.HeroAPI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShipModeEditor extends JPanel implements OptionEditor<ShipMode> {

    private HeroAPI.Configuration newConfig;

    private final List<ConfigButton> configButtons = Arrays.stream(HeroAPI.Configuration.values())
            .filter(c -> c != HeroAPI.Configuration.UNKNOWN)
            .map(ConfigButton::new)
            .collect(Collectors.toList());

    private SelectableItem.Formation newFormation;

    private final JComboBox<SelectableItem.Formation> comboBox = new JComboBox<>(SelectableItem.Formation.values());

    public ShipModeEditor() {
        super(new MigLayout("gap 0, ins 0", "[][]5px[]", "[17px]"));

        this.newConfig = HeroAPI.Configuration.FIRST;
        this.newFormation = SelectableItem.Formation.STANDARD;

        setOpaque(false);

        configButtons.forEach(button -> add(button, "wmax 17, hmax 17"));

        comboBox.setRenderer(new FormationRenderer());
        comboBox.addItemListener(item -> {
            if (item.getStateChange() == ItemEvent.SELECTED) {
                setFormation((SelectableItem.Formation) item.getItem());
            }
        });
        add(comboBox, "hmax 17");
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<ShipMode> shipConfig) {
        ShipMode value = shipConfig.getValue();
        setConfig(value.getConfiguration());
        setFormation(value.getFormation());
        return this;
    }

    private void setConfig(HeroAPI.Configuration config) {
        this.newConfig = config;
        configButtons.forEach(ConfigButton::repaint);
    }

    private void setFormation(SelectableItem.Formation newFormation) {
        this.newFormation = newFormation;
        this.comboBox.setSelectedItem(newFormation);
    }

    @Override
    public ShipMode getEditorValue() {
        return new Config.ShipConfig(newConfig, newFormation);
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
            return ShipModeEditor.this.newConfig == config;
        }
    }

    private static class FormationRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof SelectableItem.Formation) {
                SelectableItem.Formation newFormation = (SelectableItem.Formation) value;

                String iconName = newFormation.name().toLowerCase();
                Icon icon = UIUtils.getIcon("formations/" + iconName);

                setIcon(icon);
                setText(newFormation.toString());
                setIconTextGap(3);
            }

            return this;
        }
    }
}