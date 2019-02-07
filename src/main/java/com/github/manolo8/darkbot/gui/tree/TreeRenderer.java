package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class TreeRenderer extends DefaultTreeCellRenderer {

    public TreeRenderer() {
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        ConfigNode node = (ConfigNode) value;
        this.setToolTipText(node.description.isEmpty() ? null : node.description);
        String text = node.name;
        if (leaf) text += ": " + Objects.toString(value, "");
        super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);
        super.setIcon(null);
        this.setPreferredSize(new Dimension(this.getFontMetrics(getFont()).stringWidth(text) + 5, 16));
        return this;
    }
}
