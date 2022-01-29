package com.github.manolo8.darkbot.gui.utils.tree;

import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class SimpleConfigSettingRenderer extends DefaultTreeCellRenderer {

    public SimpleConfigSettingRenderer() {
        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        return super.getTreeCellRendererComponent(tree, ((ConfigSetting<?>) value).getName(), sel, expanded, leaf, row, hasFocus);
    }
}
