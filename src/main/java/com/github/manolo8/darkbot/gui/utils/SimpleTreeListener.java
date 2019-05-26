package com.github.manolo8.darkbot.gui.utils;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public interface SimpleTreeListener extends TreeModelListener {
    default void treeNodesChanged(TreeModelEvent e) {}
    default void treeNodesInserted(TreeModelEvent e){}
    default void treeNodesRemoved(TreeModelEvent e){}
}
