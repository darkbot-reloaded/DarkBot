package com.github.manolo8.darkbot.config.actions.tree;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class ParseTreeModel extends DefaultTreeModel {

    public ParseTreeModel(ParsingNode root) {
        super(root);
    }

    public void updateListeners() {
        fireTreeStructureChanged(this, null, null, null);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

}

