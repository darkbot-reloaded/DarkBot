package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.config.tree.ConfigNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class TreeEditor extends DefaultTreeCellEditor {

    private final TreeCell treeCell;

    public TreeEditor(JTree tree, EditorManager editors) {
        super(tree, new DefaultTreeCellRenderer());

        this.treeCell = new TreeCell(editors);
    }

    public JTree getTree() {
        return tree;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {
        treeCell.setEditing((ConfigNode) value);
        return treeCell;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        if (e == null || e.getSource() != tree || !(e instanceof MouseEvent)) return false;

        lastPath = tree.getClosestPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());
        return lastPath != null && tree.getModel().isLeaf(lastPath.getLastPathComponent());
    }

}
