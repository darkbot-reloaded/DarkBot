package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;

public class BooleanEditor extends JCheckBox implements OptionEditor<Boolean> {

    @Override
    public JComponent getEditorComponent(ConfigSetting<Boolean> bool) {
        setSelected(bool.getValue());
        setEnabled(!Boolean.TRUE.equals(bool.getMetadata("readonly")));
        return this;
    }

    @Override
    public Boolean getEditorValue() {
        return isSelected();
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
