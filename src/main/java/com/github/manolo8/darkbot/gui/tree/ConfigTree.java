package com.github.manolo8.darkbot.gui.tree;

import com.github.manolo8.darkbot.gui.utils.tree.ConfigSettingTreeModel;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ConfigTree extends JTree {

    public ConfigTree(ConfigSettingTreeModel model, EditorProvider renderer, EditorProvider editor) {
        super(model);
        setEditable(true);
        setInvokesStopCellEditing(true);
        setRootVisible(false);
        setShowsRootHandles(true);
        setToggleClickCount(1);
        setRowHeight(0);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        getActionMap().put("stopEditing", new StopAction());

        // Enter confirms the edition
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "stopEditing");
        // Enter starts editing
        getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");

        ToolTipManager.sharedInstance().registerComponent(this);

        setCellRenderer(new TreeRenderer(renderer));
        setCellEditor(new TreeEditor(this, editor));

        treeModel.addTreeModelListener((SimpleTreeListener) e -> {
            unfoldTopLevelTree();
            SwingUtilities.invokeLater(() -> {
                validate();
                repaint();
            });
        });
        unfoldTopLevelTree();
    }


    public void unfoldTopLevelTree() {
        for (int row = 0; row < getRowCount(); row++) {
            if (isExpanded(row)) continue;

            TreePath path = getPathForRow(row);
            if (treeModel.isLeaf(path.getLastPathComponent())) continue; // Ignore leaf nodes

            if (treeModel.getChildCount(path.getLastPathComponent()) <= 5 || // Has few children
                    (path = path.getParentPath()) == null || // Is the root, no parent
                    path.getPathCount() == 1 || // Is one-level in
                    hasNoLeaf(path.getLastPathComponent())) { // No sibling is a leaf
                expandRow(row);
            }
        }
    }

    private boolean hasNoLeaf(Object parent) {
        int children = treeModel.getChildCount(parent);
        for (int child = 0; child < children; child++)
            if (treeModel.isLeaf(treeModel.getChild(parent, child))) return false;
        return true;
    }

    private class StopAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopEditing();
        }
    }

}
