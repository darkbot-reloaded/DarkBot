package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.tree.utils.DropdownRenderer;
import com.github.manolo8.darkbot.gui.tree.utils.GenericDropdownModel;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.OptionEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public class DropdownEditor extends JComboBox<Object> implements OptionEditor<Object> {

    private final DropdownRenderer renderer;

    public DropdownEditor() {
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showPopup();
            }
        });
        setRenderer(renderer = new DropdownRenderer());
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<Object> dropdown) {
        ComboBoxModel<Object> model = dropdown.getMetadata("dropdown.model");
        if (model instanceof GenericDropdownModel)
            ((GenericDropdownModel<Object>) model).checkUpdates();

        renderer.setOptions(dropdown.getMetadata("dropdown.options"));
        setModel(dropdown.getMetadata("dropdown.model"));
        setSelectedItem(dropdown.getValue());

        return this;
    }

    public void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);

        // After closing the popup with enter, confirm the change in the editor and go to the next node in the tree
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !isPopupVisible()) {
            JTree tree = (JTree) SwingUtilities.getAncestorOfClass(JTree.class, this);
            if (tree != null) tree.stopEditing();
        }
    }

    @Override
    public Object getEditorValue() {
        return getSelectedItem();
    }

    @Override
    public void setPopupVisible(boolean v) {
        if (v && getModel() instanceof GenericDropdownModel)
            ((GenericDropdownModel<Object>) getModel()).checkUpdates();
        super.setPopupVisible(v);
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

}
