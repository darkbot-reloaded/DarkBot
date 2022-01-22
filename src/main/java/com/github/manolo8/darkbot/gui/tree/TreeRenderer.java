package com.github.manolo8.darkbot.gui.tree;

import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class TreeRenderer implements TreeCellRenderer {
    private final TreeCell treeCell;

    public TreeRenderer(EditorProvider editors) {
        this.treeCell = new TreeCell(editors, false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        treeCell.setEditing((ConfigSetting<?>) value);
        return treeCell;
    }

}
