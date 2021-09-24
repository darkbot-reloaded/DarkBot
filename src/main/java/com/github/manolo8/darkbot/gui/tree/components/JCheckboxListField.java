package com.github.manolo8.darkbot.gui.tree.components;


import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Options;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.utils.I18n;
import com.github.manolo8.darkbot.utils.ReflectionUtils;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class JCheckboxListField extends JComboBox<String> implements OptionEditor {
    private final Map<Class<? extends OptionList<?>>, OptionList<?>> optionInstances = new HashMap<>();
    private OptionList<?> options;
    private Collection<Object> elements;
    protected ConfigField field;


    private boolean keepOpen;
    private transient ActionListener listener;

    public JCheckboxListField() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
            }
        });
    }

    @Override
    public void updateUI() {
        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };

        javax.swing.JLabel label = new javax.swing.JLabel(" ");
        JCheckBox check = new JCheckBox(" ");
        check.setOpaque(true);
        setRenderer((JList<? extends String> list, String text, int index, boolean isSelected, boolean cellHasFocus) -> {
            if (index < 0) {
                label.setText(I18n.get("misc.editor.checkbox_list.selected",
                        elements.size() + "/" + options.getOptions().size()));
                return label;
            }
            check.setText(text);
            check.setToolTipText(options.getTooltip(text));
            check.setSelected(elements.contains(options.getValue(text)));
            check.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            check.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return check;
        });
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    updateItem(((ComboPopup) a).getList().getSelectedIndex());
                }
            }
        });
    }

    private void updateItem(int index) {
        if (!isPopupVisible()) return;
        String option = getItemAt(index);
        Object val = options.getValue(option);
        if (elements.contains(val)) elements.remove(val);
        else elements.add(val);
        field.set(elements); // Make it update config

        setSelectedIndex(-1);
        setSelectedItem(getItemAt(index));
    }

    @Override
    public void setPopupVisible(boolean v) {
        if (keepOpen) keepOpen = false;
        else super.setPopupVisible(v);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = null;
        options = optionInstances.computeIfAbsent(
                field.field.getAnnotation(Options.class).value(), ReflectionUtils::createInstance);
        elements = field.get();

        if (getModel() != options) setModel(options);


        this.field = field;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 180);
        return AdvancedConfig.forcePreferredHeight(d);
    }

}

