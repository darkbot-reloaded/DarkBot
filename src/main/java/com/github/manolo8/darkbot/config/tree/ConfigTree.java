package com.github.manolo8.darkbot.config.tree;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigTree implements TreeModel {

    private ConfigNode.Parent root;
    private ConfigNode[] originalChildren;
    private List<TreeModelListener> listeners = new ArrayList<>();

    public ConfigTree(Object config) {
        this.root = ConfigNode.rootingFrom(null, "Root", config, "config");
        this.originalChildren = root.children;
    }

    public void setCustom(String name, Object customConfig) {
        if (customConfig == null && root.children == originalChildren) return;
        ConfigNode[] newChildren;
        if (customConfig == null) newChildren = originalChildren;
        else {
            newChildren = Arrays.copyOf(originalChildren, originalChildren.length + 1);
            newChildren[originalChildren.length] = ConfigNode.rootingFrom(root, name, customConfig, "custom");
        }

        this.root.children = newChildren;
        SwingUtilities.invokeLater(this::updateListeners);
    }

    private void updateListeners() {
        TreeModelEvent event = new TreeModelEvent(this, (TreeNode[]) null, null, null);
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((ConfigNode.Parent) parent).children[index];
    }

    @Override
    public int getChildCount(Object parent) {
        return ((ConfigNode.Parent) parent).children.length;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof ConfigNode.Leaf;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        TreeModelEvent event = new TreeModelEvent(this, path);
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(event);
        }
    }

    @Override
    public int getIndexOfChild(Object parentObj, Object child) {
        ConfigNode.Parent parent = (ConfigNode.Parent) parentObj;
        for (int i = 0; i < parent.children.length; i++) {
            if (parent.children[i] == child) return i;
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
}
