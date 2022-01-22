package com.github.manolo8.darkbot.gui.tree;

import eu.darkbot.api.config.ConfigSetting;

import javax.swing.*;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public class TreeEditor extends AbstractCellEditor implements TreeCellEditor {

    private final JTree tree;
    private final TreeCell treeCell;

    public TreeEditor(JTree tree, EditorProvider editors) {
        this.tree = tree;
        this.treeCell = new TreeCell(editors, true);
        // Don't let focus leave the cell until it's confirmed or cancelled
        this.treeCell.setFocusCycleRoot(true);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                boolean expanded, boolean leaf, int row) {
        treeCell.setEditing((ConfigSetting<?>) value);
        return treeCell;
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        TreePath path = null;
        if (e == null) path = tree.getSelectionPath();
        else if (e.getSource() == tree && e instanceof MouseEvent)
            path = tree.getClosestPathForLocation(((MouseEvent)e).getX(), ((MouseEvent)e).getY());

        if (path == null || !tree.getModel().isLeaf(path.getLastPathComponent())) return false;
        ConfigSetting<?> config = (ConfigSetting<?>) path.getLastPathComponent();
        return !Boolean.TRUE.equals(config.getMetadata("readonly"));
    }

    @Override
    public Object getCellEditorValue() {
        return treeCell.getValue();
    }

    @Override
    public boolean stopCellEditing() {
        return treeCell.stopCellEditing() && super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        treeCell.cancelCellEditing();
        super.cancelCellEditing();
    }
}
