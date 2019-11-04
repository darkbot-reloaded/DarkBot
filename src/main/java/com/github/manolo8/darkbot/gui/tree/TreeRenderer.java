package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigNode;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.utils.I18n;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class TreeRenderer extends DefaultTreeCellRenderer {

    private int depth = 0;

    private TreeEditor delegate;
    public void setDelegateEditor(TreeEditor editor) {
        this.delegate = editor;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        ConfigNode node = (ConfigNode) value;
        depth = node.getDepth();

        if (!leaf || ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.DISPLAY.HIDE_EDITORS) {
            String text = I18n.getOrDefault(node.key, node.name);
            if (leaf) {
                String val = Objects.toString(node.toString(), "(unset)");
                if (!text.isEmpty() && !val.isEmpty()) text += ": ";
                text += val;
            }
            setToolTipText(I18n.getOrDefault(node.key + ".desc", node.description));
            super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);
            return this;
        } else {
            return delegate.getTreeCellEditorComponent(tree, value, sel, expanded, true, row);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = depth <= 1 ? AdvancedConfig.HEADER_HEIGHT : AdvancedConfig.ROW_HEIGHT;
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
