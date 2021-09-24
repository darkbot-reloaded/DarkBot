package com.github.manolo8.darkbot.gui.tree.utils;

import com.github.manolo8.darkbot.gui.tree.editors.MultiDropdownEditor;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.config.annotations.Dropdown;

import javax.swing.*;
import java.awt.*;

public class MultiDropdownRenderer implements ListCellRenderer<Object> {

    private final MultiDropdownEditor editor;
    private Dropdown.Options<Object> options;

    private final javax.swing.JLabel label = new javax.swing.JLabel(" ");
    private final JCheckBox check = new JCheckBox(" ");

    public MultiDropdownRenderer(MultiDropdownEditor editor) {
        this.editor = editor;
        check.setOpaque(true);
    }

    public void setOptions(Dropdown.Options<Object> options) {
        this.options = options;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0) {
            label.setText(I18n.get("misc.editor.checkbox_list.selected",
                    editor.getEditorValue().size() + "/" + list.getModel().getSize()));
            return label;
        }
        check.setText(options.getText(value));
        check.setToolTipText(options.getTooltip(value));
        check.setSelected(editor.getEditorValue().contains(value));
        check.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        check.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        return check;
    }
}
