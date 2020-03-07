package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;

import javax.swing.*;
import java.awt.*;

public class JRangeField extends JPanel implements OptionEditor {

    private Config.PercentRange field;
    private JSpinner min = createPercentSpinner(), max = createPercentSpinner();

    public JRangeField() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        min.addChangeListener(e -> {
            if (field != null) field.min = (double) min.getValue();
            if ((double) max.getValue() < (double) min.getValue()) {
                max.setValue(min.getValue());
            }
        });

        max.addChangeListener(e -> {
            if (field != null) field.max = (double) max.getValue();
            if ((double) max.getValue() < (double) min.getValue()) {
                min.setValue(max.getValue());
            }
        });
        add(min);
        add(new javax.swing.JLabel(" - "));
        add(max);
    }

    private JSpinner createPercentSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberMinMaxFix(0, 0, 1, 0.05)) {
            @Override
            public Dimension getPreferredSize() {
                return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
            }
        };
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0%"));
        return spinner;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        Config.PercentRange percent = field.get();
        min.setValue(percent.min);
        max.setValue(percent.max);
        this.field = percent;
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
