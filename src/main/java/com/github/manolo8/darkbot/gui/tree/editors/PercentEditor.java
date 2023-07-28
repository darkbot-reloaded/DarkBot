package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.SpinnerUtils;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;

public class PercentEditor extends JSpinner implements OptionEditor<Double> {

    public PercentEditor() {
        super(new SpinnerNumberMinMaxFix(0d, 0d, 1d, 0.05d));
        setEditor(new JSpinner.NumberEditor(this, "0%"));
        ((DefaultEditor) getEditor()).getTextField().setColumns(3);
        addChangeListener(a -> SpinnerUtils.setError(this, false));
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Double> percent) {
        setValue(percent.getValue());
        setEnabled(!Boolean.TRUE.equals(percent.getMetadata("readonly")));
        return this;
    }

    @Override
    public boolean stopCellEditing() {
        return SpinnerUtils.tryStopEditing(this);
    }

    @Override
    public Double getEditorValue() {
        return (Double) getValue();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
