package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

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
    private int delay = 250;

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

                if (e.getClickCount() == 2) editValue(true);
            }
        });

        addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                if (path.getLastPathComponent() instanceof ObjectTreeNode) {
                    ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
                    node.loadChildren(InspectorTree.this);
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
                    timer = new Timer(delay, e -> {
                        ((ObjectTreeNode) model.getRoot()).update(InspectorTree.this);
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

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    public void setTimerDelay(int delay) {
        this.delay = delay;
        if (timer != null) timer.setDelay(delay);
    }

    private ObjectTreeNode getSelectedNode() {
        TreePath path = getSelectionPath();
        if (path == null) return null;
        Object node = path.getLastPathComponent();
        return node instanceof ObjectTreeNode ? (ObjectTreeNode) node : null;
    }

    private void editValue(boolean primitivesOnly) {
        ObjectTreeNode node = getSelectedNode();
        if (node != null && node.isMemoryWritable()) {
            ObjectInspector.Slot slot = (ObjectInspector.Slot) node.getUserObject();
            if (primitivesOnly && slot.slotType == ObjectInspector.Slot.Type.OBJECT) return;

            String result = JOptionPane.showInputDialog(getRootPane(),
                    "Edit value of " + slot.type + " " + slot.name, "Edit value", JOptionPane.PLAIN_MESSAGE);
            if (result != null && !result.isEmpty())
                node.memoryWrite(result);
        }
    }

    private class ContextMenu extends JPopupMenu {

        public ContextMenu() {
            super("Node");

            JMenuItem copyValueItem = new JMenuItem("Copy value");
            JMenuItem copyAddressItem = new JMenuItem("Copy address");
            JMenuItem editValueItem = new JMenuItem("Edit value");
            copyValueItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(node.strValue);
            });
            copyAddressItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(String.format("0x%x", node.address.get()));
            });
            editValueItem.addActionListener(a -> editValue(false));
            add(copyValueItem);
            add(copyAddressItem);
            add(editValueItem);
        }
    }

}