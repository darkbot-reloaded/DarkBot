package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.tree.handlers.NumberHandler;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.SpinnerUtils;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.ParseException;

public class NumberEditor extends JPanel implements OptionEditor<Number> {

    private final JCheckBox checkBox = new BooleanEditor();
    private final NumberSpinner spinner = new NumberSpinner();

    private Number disabledValue;

    public NumberEditor() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        setOpaque(false);

        add(checkBox);
        checkBox.addChangeListener(e -> {
            spinner.setEnabled(checkBox.isSelected());
            SpinnerUtils.setError(spinner, false);
        });
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Number> number) {
        disabledValue = MathUtils.toNumber(number.getMetadata("disabled"), number.getType());

        checkBox.setSelected(disabledValue == null || !disabledValue.equals(number.getValue()));
        spinner.setEditing(number);
        if (disabledValue == null) return spinner;

        add(spinner); // Need to re-add because it's auto-removed if displayed alone
        return this;
    }

    @Override
    public boolean stopCellEditing() {
        return SpinnerUtils.tryStopEditing(spinner);
    }

    @Override
    public Number getEditorValue() {
        if (disabledValue != null && !checkBox.isSelected()) return disabledValue;
        else return spinner.model.getNumber();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    private class NumberSpinner extends JSpinner {
        private final NumberFormatEditor integer, decimal;
        private SpinnerNumberMinMaxFix model;

        public NumberSpinner() {
            integer = new NumberFormatEditor("");
            decimal = new NumberFormatEditor("0.0");
        }

        public void setEditing(ConfigSetting<Number> number) {
            Double min = number.getMetadata("min"),
                    max = number.getMetadata("max"),
                    step = number.getMetadata("step");
            if (min == null || max == null || step == null)
                throw new UnsupportedOperationException("Min, max & step metadata must not be missing");

            Class<? extends Number> type = number.getType();

            NumberFormatEditor nfe = type == Double.class || type == Float.class ? decimal : integer;

            Number value = number.getValue();
            if (disabledValue != null && disabledValue.equals(value)) {
                value = MathUtils.toNumber(number.getMetadata("disabled-default"), number.getType());
                if (value == null)
                    throw new IllegalArgumentException("Disabled was set without a disabled-default!");
            }

            setEditor(nfe.editor);
            setModel(model = new SpinnerNumberMinMaxFix(
                    NumberHandler.enforceLimit(value, min, max),
                    MathUtils.toComparable(min, type),
                    MathUtils.toComparable(max, type),
                    MathUtils.toNumber(step, type)));
            fireStateChanged(); // Force editor to pick up new value

            int length;
            try {
                length = nfe.formatter.valueToString(max).length();
            } catch (ParseException e) {
                length = String.valueOf(number.getValue()).length();
            }

            setPreferredSize(new Dimension(25 + (length * 9), AdvancedConfig.EDITOR_HEIGHT));
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
                this.editor = new JSpinner.NumberEditor(NumberSpinner.this, format);
                this.editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
            }
        }

    }

}
