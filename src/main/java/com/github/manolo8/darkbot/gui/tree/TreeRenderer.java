package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.AdvancedConfig;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class TreeRenderer extends DefaultTreeCellRenderer {

    private boolean leaf;

    public TreeRenderer() {
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        this.leaf = leaf;
        ConfigNode node = (ConfigNode) value;
        setToolTipText(node.description.isEmpty() ? null : node.description);
        String text = node.name + (leaf ? ": " + Objects.toString(value, "") : "");
        super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, false);
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = leaf ? AdvancedConfig.ROW_HEIGHT : AdvancedConfig.HEADER_HEIGHT;
        return d;
    }

    @Override
    public Color getBackgroundSelectionColor() {
        return null;
    }

    @Override
    public Color getBackgroundNonSelectionColor() {
        return null;
    }

    public Icon getIcon() {
        return null;
    }

}
