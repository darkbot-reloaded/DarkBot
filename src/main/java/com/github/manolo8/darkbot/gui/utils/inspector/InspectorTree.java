package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
            public void treeWillExpand(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path.getLastPathComponent() instanceof ObjectTreeNode) {
                    ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
                    node.loadChildren(InspectorTree.this);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path.getLastPathComponent() instanceof ObjectTreeNode) {
                    ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
                    node.unloadChildren(InspectorTree.this);
                }
            }
        });

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (timer == null) {
                    timer = new Timer(delay, e -> {
                        ((ObjectTreeNode) model.getRoot()).update(InspectorTree.this);
                        if (ObjectTreeNode.maxTextLengthChanged())
                            notifyNodesChanged(model, (TreeNode) model.getRoot());

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

        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setCellRenderer(new InspectorTreeCellRenderer());
    }

    private static void notifyNodesChanged(DefaultTreeModel treeModel, TreeNode node) {
        treeModel.nodeChanged(node);
        node.children().asIterator()
                .forEachRemaining(c -> notifyNodesChanged(treeModel, c));
    }

    @Override
    public boolean hasBeenExpanded(TreePath path) {
        return false;
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
            ObjectInspector.Slot slot = node.getSlot();
            if (primitivesOnly && slot.slotType == ObjectInspector.Slot.Type.OBJECT) return;

            String result = JOptionPane.showInputDialog(getRootPane(),
                    "Edit value of " + slot.type + " " + slot.name, "Edit value", JOptionPane.PLAIN_MESSAGE);
            if (result != null && !result.isEmpty())
                node.memoryWrite(result);
        }
    }

    private static class InspectorTreeCellRenderer extends DefaultTreeCellRenderer {
        private Color bgColor;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component treeCellRendererComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            ObjectTreeNode node = (ObjectTreeNode) value;

            bgColor = node.getBackgroundColor(sel ? super.getBackgroundSelectionColor() : super.getBackgroundNonSelectionColor());
            return treeCellRendererComponent;
        }

        @Override
        public Color getBackgroundNonSelectionColor() {
            return bgColor == null ? super.getBackgroundNonSelectionColor() : bgColor;
        }

        @Override
        public Color getBackgroundSelectionColor() {
            return bgColor == null ? super.getBackgroundSelectionColor() : bgColor;
        }
    }

    private class ContextMenu extends JPopupMenu {

        public ContextMenu() {
            super("Node");

            JMenuItem copyValueItem = new JMenuItem("Copy value");
            JMenuItem copyAddressItem = new JMenuItem("Copy address");
            JMenuItem copyClosureAddressItem = new JMenuItem("Copy class address");
            JMenuItem copyClassStructureItem = new JMenuItem("Copy class structure");
            JMenuItem editValueItem = new JMenuItem("Edit value");
            JMenuItem editTypeNameItem = new JMenuItem("Edit type name");
            copyValueItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(node.getText());
            });
            copyAddressItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) SystemUtils.toClipboard(String.format("0x%x", node.address.getAsLong()));
            });

            copyClosureAddressItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) {
                    ObjectInspector.Slot slot = node.getSlot();
                    if (slot.slotType == ObjectInspector.Slot.Type.OBJECT && !slot.name.contains("$")) {
                        SystemUtils.toClipboard(String.format("0x%x", ByteUtils.getClassClosure(node.value)));
                    }
                }
            });

            copyClassStructureItem.addActionListener(this::generateClass);
            editValueItem.addActionListener(a -> editValue(false));
            editTypeNameItem.addActionListener(a -> {
                ObjectTreeNode node = getSelectedNode();
                if (node != null) {
                    ObjectInspector.Slot slot = node.getSlot();
                    String result = JOptionPane.showInputDialog(getRootPane(),
                            "Edit type name of " + slot.type + " " + slot.name, "Edit type name", JOptionPane.PLAIN_MESSAGE);
                    if (result != null && !result.isEmpty())
                        slot.setReplacement(result);
                }
            });

            copyValueItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
            copyAddressItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
            copyClosureAddressItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
            copyClassStructureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
            editValueItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));

            add(copyValueItem);
            add(copyAddressItem);
            add(copyClosureAddressItem);
            add(copyClassStructureItem);
            add(editValueItem);
            add(editTypeNameItem);
        }

        private void generateClass(ActionEvent e) {
            ObjectTreeNode node = getSelectedNode();
            if (node != null) {
                ObjectInspector.Slot slot = node.getSlot();
                if (slot.slotType == ObjectInspector.Slot.Type.OBJECT
                        || slot.slotType == ObjectInspector.Slot.Type.PLAIN_OBJECT) {

                    String generated = ClassGenerator.generate(slot.name.replace("-", ""),
                            ObjectInspector.getObjectSlots(node.value));
                    SystemUtils.toClipboard(generated);
                }
            }
        }
    }
}