package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.SizedLabel;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;

public class RangeEditor extends JPanel implements OptionEditor<PercentRange> {

    private final PercentEditor min, max;

    public RangeEditor() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        add(min = new PercentEditor());
        add(new SizedLabel(" - "));
        add(max = new PercentEditor());

        min.addChangeListener(e -> {
            if (min.getEditorValue() > max.getEditorValue()) max.setValue(min.getValue());
        });
        max.addChangeListener(e -> {
            if (max.getEditorValue() < min.getEditorValue()) min.setValue(max.getValue());
        });
        setOpaque(false);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<PercentRange> percentRange) {
        PercentRange range = percentRange.getValue();
        min.setValue(range.getMin());
        max.setValue(range.getMax());
        return this;
    }

    @Override
    public PercentRange getEditorValue() {
        return new Config.PercentRange(min.getEditorValue(), max.getEditorValue());
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
