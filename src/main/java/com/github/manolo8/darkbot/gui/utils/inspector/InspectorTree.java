package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.utils.SystemUtils;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InspectorTree extends JTree {

    private final JPopupMenu popupMenu = new ContextMenu();
    private Timer timer;

    public InspectorTree(DefaultTreeModel model) {
        super(model);
        setShowsRootHandles(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    setSelectionRow(row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                if (path.getLastPathComponent() instanceof ObjectTreeNode) {
                    ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
                    node.loadChildren(model);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (timer == null) {
                    timer = new Timer(250, e -> {
                        ((ObjectTreeNode) model.getRoot()).update(model);
                        invalidate();
                    });
                    timer.setRepeats(true);
                    timer.start();
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                timer.stop();
                timer = null;
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {

            }
        });

        DefaultTreeCellRenderer cellRender = new DefaultTreeCellRenderer();
        cellRender.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setCellRenderer(cellRender);
    }

    private ObjectTreeNode getSelectedNode() {
        TreePath path = getSelectionPath();
        if (path == null) return null;
        Object node = path.getLastPathComponent();
        return node instanceof ObjectTreeNode ? (ObjectTreeNode) node : null;
    }

    private class ContextMenu extends JPopupMenu {

        public ContextMenu() {
            super("Node");

            JMenuItem copyValueItem = new JMenuItem("Copy value");
            JMenuItem copyAddressItem = new JMenuItem("Copy address");
            copyValueItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(node.strValue);
            });
            copyAddressItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(String.format("0x%x", node.address));
            });
            add(copyValueItem);
            add(copyAddressItem);
        }
    }

}