package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class TreeRenderer implements TreeCellRenderer {
    private final TreeCell treeCell;

    public TreeRenderer(EditorManager editors) {
        this.treeCell = new TreeCell(editors);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        treeCell.setEditing((ConfigNode) value);
        return treeCell;
    }

}
