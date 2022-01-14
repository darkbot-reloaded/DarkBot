package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.DropdownRenderer;
import com.github.manolo8.darkbot.gui.tree.utils.GenericDropdownModel;
import com.github.manolo8.darkbot.gui.tree.utils.MultiDropdownRenderer;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MultiDropdownEditor extends JComboBox<Object> implements OptionEditor<Set<Object>> {

    private MultiDropdownRenderer renderer;
    private ActionListener listener;

    private ConfigSetting<Set<Object>> setting;
    private Set<Object> elements = new HashSet<>();

    private boolean keepOpen;


    public MultiDropdownEditor() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
            }
        });
    }

    @Override
    public void updateUI() {
        // It'd make a lot of sense to define these as final and set them on the constructor.
        // However, the parent constructor calls this, meaning our constructor would be too late.
        if (renderer == null) renderer = new MultiDropdownRenderer(this);
        if (listener == null) listener =  e -> {
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };

        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();

        setRenderer(renderer);
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) updateItem(((ComboPopup) a).getList().getSelectedIndex());
            }
        });
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Set<Object>> dropdown) {
        ComboBoxModel<Object> model = dropdown.getMetadata("dropdown.model");
        if (model instanceof GenericDropdownModel)
            ((GenericDropdownModel<Object>) model).checkUpdates();

        if (setting == dropdown && elements.equals(dropdown.getValue())) return this;
        setting = dropdown;
        elements = copyOf(dropdown);

        renderer.setOptions(dropdown.getMetadata("dropdown.options"));
        setModel(model);
        setSelectedItem(dropdown.getValue());

        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Set<Object> copyOf(ConfigSetting<Set<Object>> setting) {
        if (EnumSet.class.isAssignableFrom(setting.getType()))
            return EnumSet.copyOf((EnumSet) setting.getValue());

        return new HashSet<>(setting.getValue());
    }

    private void updateItem(int index) {
        if (!isPopupVisible()) return;
        Object val = getItemAt(index);
        if (elements.contains(val)) elements.remove(val);
        else elements.add(val);

        setSelectedIndex(-1);
        setSelectedItem(val);
    }

    @Override
    public Set<Object> getEditorValue() {
        return elements;
    }

    @Override
    public void setPopupVisible(boolean v) {
        if (v && getModel() instanceof GenericDropdownModel)
            ((GenericDropdownModel<Object>) getModel()).checkUpdates();
        if (keepOpen) keepOpen = false;
        else super.setPopupVisible(v);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 180);
        return AdvancedConfig.forcePreferredHeight(d);
    }

}
