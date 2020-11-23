package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.config.ConfigEntity;

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

    private final ConfigNode.Parent root;
    private final ConfigNode[] originalChildren;
    private final List<TreeModelListener> listeners = new ArrayList<>();

    private final TreeFilter filter = new TreeFilter();

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

    public int getTopLevelChildrenCount() {
        return root.children.length;
    }

    public ConfigNode getTopLevelChild(int idx) {
        return idx < 0 || idx >= root.children.length ? null : root.children[idx];
    }

    public TreeFilter getFilter() {
        return filter;
    }

    public void updateListeners() {
        TreeModelEvent event = new TreeModelEvent(this, (TreeNode[]) null, null, null);
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }

    @Override
    public Object getRoot() {
        if (filter.isUnfiltered() && ConfigEntity.INSTANCE.getConfig().BOT_SETTINGS.BOT_GUI.CONFIG_TREE_TABS) {
            int i = filter.getSelectedRoot();
            if (i >= 0 && i < root.children.length) return root.children[i];
        }
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (filter.isUnfiltered()) return ((ConfigNode.Parent) parent).children[index];
        return Arrays.stream(((ConfigNode.Parent) parent).children)
                .filter(n -> n.isVisible(filter))
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getChildCount(Object parent) {
        return isLeaf(parent) ? 0 :
                filter.isUnfiltered() ? ((ConfigNode.Parent) parent).children.length :
                        (int) Arrays.stream(((ConfigNode.Parent) parent).children)
                                .filter(n -> n.isVisible(filter))
                                .count();
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
        int idx = -1;
        for (ConfigNode ch : parent.children) {
            if (filter.isUnfiltered() || ch.isVisible(filter)) {
                idx++;
                if (ch == child) return idx;
            }
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
