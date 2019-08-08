package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.gui.tree.components.JListField;

import javax.swing.*;
import java.awt.*;

public class ToolTipListRenderer extends DefaultListCellRenderer {

    private final JListField field;

    public ToolTipListRenderer(JListField field) {
        this.field = field;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected && value instanceof String) setToolTipText(field.getToolTipFor((String) value));
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
