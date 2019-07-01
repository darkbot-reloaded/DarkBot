package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.AdvancedConfig;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TreeRenderer extends DefaultTreeCellRenderer {

    private int depth = 0;

    private TreeEditor delegate;
    public void setDelegateEditor(TreeEditor editor) {
        this.delegate = editor;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        if (delegate == null) delegate = new TreeEditor(tree, this);
        ConfigNode node = (ConfigNode) value;
        depth = node.getDepth();

        setToolTipText(node.description.isEmpty() ? null : node.description);
        if (!leaf) {
            super.getTreeCellRendererComponent(tree, node.name, sel, expanded, false, row, hasFocus);
            return this;
        } else {
            return delegate.getTreeCellEditorComponent(tree, value, sel, expanded, true, row);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = depth <= 1 ? AdvancedConfig.HEADER_HEIGHT : AdvancedConfig.EDITOR_HEIGHT + 2;
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
