package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;

public class NumberEditor extends JSpinner implements OptionEditor<Number> {

    private final NumberFormatEditor integer, decimal, percent;

    private SpinnerNumberMinMaxFix model;

    public NumberEditor() {
        integer = new NumberFormatEditor("");
        decimal = new NumberFormatEditor("0.0");
        percent = new NumberFormatEditor("0%");
    }

    @Override
    public JComponent getEditorComponent(Number value, ValueHandler<Number> handler) {
        Double min = handler.getMetadata("min"),
                max = handler.getMetadata("max"),
                step = handler.getMetadata("step");
        if (min == null || max == null || step == null)
            throw new UnsupportedOperationException("Min, max & step metadata must not be missing");

        Class<? extends Number> type = value.getClass();

        NumberFormatEditor nfe = Boolean.TRUE.equals(handler.getMetadata("isPercent")) ? percent :
                type == Double.class || type == Float.class ? decimal : integer;

        setEditor(nfe.editor);
        setModel(model = new SpinnerNumberMinMaxFix(value,
                MathUtils.toComparable(min, type),
                MathUtils.toComparable(max, type),
                MathUtils.toNumber(step, type)));
        fireStateChanged(); // Force editor to pick up new value

        int length;
        try {
            length = nfe.formatter.valueToString(max).length();
        } catch (ParseException e) {
            length = String.valueOf(value).length();
        }

        setPreferredSize(new Dimension(25 + (length * 9), AdvancedConfig.EDITOR_HEIGHT));

        return this;
    }

    @Override
    public Number getEditorValue() {
        return model.getNumber();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    private class NumberFormatEditor {
        private final NumberFormatter formatter;
        private final JSpinner.NumberEditor editor;

        public NumberFormatEditor(String format) {
            this.formatter = new NumberFormatter(new DecimalFormat(format));
            this.editor = new JSpinner.NumberEditor(
                    com.github.manolo8.darkbot.gui.tree.editors.NumberEditor.this, format);
        }
    }

}
