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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class MultiDropdownEditor extends JComboBox<Object> implements OptionEditor<Set<Object>> {

    private final MultiDropdownRenderer renderer;

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

        setRenderer(renderer = new MultiDropdownRenderer(this));
        addActionListener(e -> {
            if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
                if (!isPopupVisible()) return;
                setSelectedIndex(-1);
                setSelectedItem(getSelectedIndex());
                keepOpen = true;
            }
        });
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    if (!isPopupVisible()) return;
                    setSelectedIndex(-1);
                    setSelectedItem(((ComboPopup) a).getList().getSelectedIndex());
                }
            }
        });
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Set<Object>> dropdown) {
        ComboBoxModel<Object> model = dropdown.getHandler().getMetadata("dropdown.model");
        if (model instanceof GenericDropdownModel)
            ((GenericDropdownModel<Object>) model).checkUpdates();

        if (this.setting == dropdown) return this;
        this.setting = dropdown;

        elements = new HashSet<>();
        elements.addAll(dropdown.getValue());

        renderer.setOptions(dropdown.getHandler().getMetadata("dropdown.options"));
        setModel(model);
        setSelectedItem(dropdown.getValue());

        return this;
    }

    @Override
    public Set<Object> getEditorValue() {
        return elements;
    }

    @Override
    public void setPopupVisible(boolean v) {
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
