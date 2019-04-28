package com.github.manolo8.darkbot.gui.tree.components;

import com.bulenkov.iconloader.util.Gray;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class JShipConfigField extends JPanel implements OptionEditor {

    private JButton config1 = new ConfigButton(1), config2 = new ConfigButton(2);
    private FormationField formation = new FormationField();

    private Config.ShipConfig editing;
    private long valueSet;

    public JShipConfigField() {
        super(new MigLayout("ins 0, gap 0", "[]5[][]10[]5[]"));

        add(new JLabel("Config"));
        add(config1);
        add(config2);
        add(new JLabel("Formation"));
        add(formation);
    }

    private void setConfig(int num) {
        config1.setBackground(num == 1 ? UIManager.getColor("Tree.selectionBackground") : UIManager.getColor("Button.background"));
        config2.setBackground(num == 2 ? UIManager.getColor("Tree.selectionBackground") : UIManager.getColor("Button.background"));
        if (editing != null) this.editing.CONFIG = num;
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
        formation.setText(conf.FORMATION + "");
        formation.requestFocus();

        valueSet = System.currentTimeMillis();
        this.editing = conf;
    }

    private class ConfigButton extends JButton {
        ConfigButton(int config) {
            super(config + "");
            putClientProperty("JButton.buttonType", "square");
            setBorder(BorderFactory.createLineBorder(Gray._80));
            setMaximumSize(new Dimension(1000, 16));
            setFocusable(false);

            addActionListener(a -> setConfig(config));
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (System.currentTimeMillis() - valueSet > 10) super.processMouseEvent(e);
        }

        @Override
        public Insets getInsets() {
            return new Insets(5, 5, 5, 5);
        }
    }

    private class FormationField extends JCharField {

        @Override
        protected void setValue(Character value) {
            if (editing != null) editing.FORMATION = value;
        }

    }
}
