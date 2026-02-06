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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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

    private final JComboBox<Object> comboBox;

    public ShipModeEditor() {
        super(new MigLayout("gap 0, ins 0", "[][]5px[]", "[17px]"));

        this.newConfig = HeroAPI.Configuration.FIRST;
        this.newFormation = SelectableItem.Formation.STANDARD;

        setOpaque(false);

        configButtons.forEach(button -> add(button, "wmax 17, hmax 17"));

        // Build combo box items: "None" + all formations
        Object[] items = new Object[SelectableItem.Formation.values().length + 1];
        items[0] = "None";
        System.arraycopy(SelectableItem.Formation.values(), 0, items, 1, SelectableItem.Formation.values().length);

        comboBox = new JComboBox<>(items);
        comboBox.setRenderer(new FormationRenderer());

        // FIX: Add focus listener to show popup immediately
        comboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                comboBox.showPopup();
            }
        });

        comboBox.addItemListener(item -> {
            if (item.getStateChange() == ItemEvent.SELECTED) {
                Object selected = item.getItem();
                if (selected instanceof SelectableItem.Formation) {
                    setFormation((SelectableItem.Formation) selected);
                } else {
                    setFormation(null); // "None" selected
                }
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
        this.comboBox.setSelectedItem(newFormation == null ? "None" : newFormation);
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
                SelectableItem.Formation formation = (SelectableItem.Formation) value;
                setIcon(UIUtils.getFormationIcon(formation.name().toLowerCase()));
                setText(formation.toString());
            } else {
                // "None" or any other value
                setIcon(UIUtils.getFormationIcon("default"));
                setText(String.valueOf(value));
            }

            setIconTextGap(3);
            return this;
        }
    }
}