package com.github.manolo8.darkbot.gui.tree.utils;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.annotations.Dropdown;

import javax.swing.*;
import java.awt.*;

public class DropdownRenderer extends DefaultListCellRenderer {

    private Dropdown.Options<Object> options;

    public static <T extends Enum<T>> DropdownRenderer ofEnum(PluginAPI api, Class<T> e, String baseKey) {
        EnumDropdownOptions<T> op = new EnumDropdownOptions<>(api, null, e, baseKey);
        DropdownRenderer r = new DropdownRenderer();

        //noinspection unchecked,rawtypes
        r.setOptions((Dropdown.Options) op);
        return r;
    }

    public void setOptions(Dropdown.Options<Object> options) {
        this.options = options;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        if (options != null) {
            if (isSelected) setToolTipText(options.getTooltip(value));
            value = options.getText(value);
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
