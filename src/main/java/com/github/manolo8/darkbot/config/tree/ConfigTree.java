package com.github.manolo8.darkbot.config.tree;

import com.github.manolo8.darkbot.config.Config;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class ConfigTree implements TreeModel {

    private ConfigNode root;

    public ConfigTree(Config config) {
        this.root = ConfigNode.root(config);
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
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
