package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;

public class PercentEditor extends JSpinner implements OptionEditor<Double> {

    public PercentEditor() {
        super(new SpinnerNumberMinMaxFix(0, 0d, 1d, 0.05d));
        setEditor(new JSpinner.NumberEditor(this, "0%"));
        ((DefaultEditor) getEditor()).getTextField().setColumns(3);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Double> percent) {
        setValue(percent.getValue());
        return this;
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
