package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.tree.handlers.NumberHandler;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.SpinnerUtils;
import com.github.manolo8.darkbot.gui.utils.SpinnerNumberMinMaxFix;
import com.github.manolo8.darkbot.utils.MathUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class NumberEditor extends JPanel implements OptionEditor<Number> {

    private final JCheckBox checkBox = new BooleanEditor();
    private final Map<Class<? extends Number>, NumberSpinner<?>> spinners = new HashMap<>();
    private NumberSpinner<?> spinner;

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
        if (this.spinner != null) remove(spinner);

        this.spinner = spinners.computeIfAbsent(number.getType(), NumberSpinner::new);

        disabledValue = MathUtils.toNumber(number.getMetadata("disabled"), number.getType());

        boolean isEnabled = disabledValue == null || !disabledValue.equals(number.getValue());
        checkBox.setSelected(isEnabled);
        spinner.setEnabled(isEnabled);
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
        else return (Number) spinner.getValue();
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    private class NumberSpinner<T extends Number> extends JSpinner {
        private final DecimalFormat format;
        private final double avgCharWidth;

        public NumberSpinner(Class<T> type) {
            super(new SpinnerNumberMinMaxFix(
                    MathUtils.toNumber(0, type), null, null, MathUtils.toNumber(1, type)));

            NumberEditor editor = (NumberEditor) getEditor();
            this.format = editor.getFormat();
            editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
            avgCharWidth = editor.getFontMetrics(editor.getFont()).stringWidth("1234567890") * 0.105d;
        }

        public void setEditing(ConfigSetting<Number> number) {
            Double min = number.getMetadata("min"),
                    max = number.getMetadata("max"),
                    step = number.getMetadata("step");
            if (min == null || max == null || step == null)
                throw new UnsupportedOperationException("Min, max & step metadata must not be missing");

            Class<? extends Number> type = number.getType();

            Number value = number.getValue();
            if (disabledValue != null && disabledValue.equals(value)) {
                value = MathUtils.toNumber(number.getMetadata("disabled-default"), number.getType());
                if (value == null)
                    throw new IllegalArgumentException("Disabled was set without a disabled-default!");
            }

            setModel(new SpinnerNumberMinMaxFix(
                    NumberHandler.enforceLimit(value, min, max),
                    MathUtils.toComparable(min, type),
                    MathUtils.toComparable(max, type),
                    MathUtils.toNumber(step, type)));
            fireStateChanged(); // Force editor to pick up new value

            double width = Math.ceil(format.format(max).length() * avgCharWidth);
            setPreferredSize(new Dimension(33 + (int) width, AdvancedConfig.EDITOR_HEIGHT));
        }

        @Override
        public Dimension getPreferredSize() {
            return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
        }

    }

}
