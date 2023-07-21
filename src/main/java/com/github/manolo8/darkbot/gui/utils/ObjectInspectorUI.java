package com.github.manolo8.darkbot.gui.utils;

import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.SystemUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import static com.github.manolo8.darkbot.Main.API;


public class ObjectInspectorUI extends JPanel {

    private final JTextField addressField;

    private JTree treeView;

    private JPopupMenu popupMenu = new JPopupMenu("Node");
    private Timer timer;

    public ObjectInspectorUI() {
        this.treeView = new JTree(new DefaultTreeModel(new ObjectTreeNode(new ObjectInspector.Slot("", "", null, 0, 0), 0, true)));
        this.addressField = new JTextField();

        treeView.setShowsRootHandles(true);


        this.addressField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    long object = Long.parseLong(addressField.getText(), 16);
                    String objectName = ByteUtils.readObjectName(object);

                    if (objectName != "ERROR") {
                        DefaultTreeModel model =  (DefaultTreeModel)treeView.getModel();
                        model.setRoot(new ObjectTreeNode(new ObjectInspector.Slot(objectName, "Object", null, 0, 8), object, true));
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid " + addressField.getText());
                }
            }
        });

        this.treeView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = treeView.getClosestRowForLocation(e.getX(), e.getY());
                    TreePath path = treeView.getClosestPathForLocation(e.getX(), e.getY());
                    if (path.getLastPathComponent() instanceof ObjectTreeNode && row > 0) {
                        ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();

                        popupMenu.removeAll();

                        String strValueCopy = node.strValue;
                        String addressCopy = String.format("0x%x", node.address);

                        JMenuItem copyValueItem = new JMenuItem("Copy value");
                        JMenuItem copyAddressItem = new JMenuItem("Copy address");

                        copyValueItem.addActionListener(a -> SystemUtils.toClipboard(strValueCopy) );
                        copyAddressItem.addActionListener(a -> SystemUtils.toClipboard(addressCopy) );

                        popupMenu.add(copyValueItem);
                        popupMenu.add(copyAddressItem);

                        treeView.setSelectionRow(row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        treeView.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                if (path.getLastPathComponent() instanceof ObjectTreeNode) {
                    ObjectTreeNode node = (ObjectTreeNode) path.getLastPathComponent();
                    node.loadChildren((DefaultTreeModel)treeView.getModel());
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException { }
        });

        DefaultTreeCellRenderer cellRender = new DefaultTreeCellRenderer();
        cellRender.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        treeView.setCellRenderer(cellRender);

        timer = new Timer(250, e -> {
            ((ObjectTreeNode)treeView.getModel().getRoot()).update((DefaultTreeModel) treeView.getModel());
            treeView.invalidate();
        });

        timer.setRepeats(true);
        timer.start();

        setLayout(new BorderLayout());
        JPanel content = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(content);
        content.setLayout(new BorderLayout());
        content.add(this.addressField, BorderLayout.NORTH);
        content.add(this.treeView, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.CENTER);
    }
}

class ObjectTreeNode extends DefaultMutableTreeNode {

    public long address;

    protected long value;

    protected String strValue;

    private Boolean addressIsValue = false;

    ObjectTreeNode(ObjectInspector.Slot slot, long address, Boolean addressIsValue) {
        super(slot);
        this.address = address;
        this.addressIsValue = addressIsValue;
    }

    @Override
    public boolean isLeaf() {
        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());
        return value == 0 || slot.size < 8 || slot.type.equals("Number") || slot.type.equals("String");
    }

    @Override
    public String toString() {
        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());
        return String.format("%03X %-25s  %-25s %s", slot.offset, (slot.name.length() >= 25) ? slot.name.substring(25) : slot.name, slot.type, strValue);
    }

    public void update(DefaultTreeModel model) {
        if (address == 0) {
            return;
        }

        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());

        if (addressIsValue) {
            value = this.address;
        } else {
            if (slot.size == 8) {
                value = API.readMemoryLong(this.address);
            } else if (slot.size == 4) {
                value = API.readMemoryInt(this.address);
            } else if (slot.size == 1) {
                value = API.readMemoryBoolean(this.address) == true ? 1 : 0;
            }
        }

        if (slot.type.equals("Number")) {
            strValue = String.format("%.2f", Double.longBitsToDouble(value));
        } else if (slot.type.equals("String")) {
            strValue = String.format("%s", API.readString(value));
        } else if (slot.type.equals("Boolean")) {
            strValue = (value == 1) ? "true" : "false";
        } else if (slot.size == 4) {
            strValue = String.format("%d", this.value);
        } else {
            strValue = String.format("0x%x", this.value);
        }

        if (children != null) {
            children.forEach(child -> {
                ((ObjectTreeNode) child).update(model);
            });
        }
        model.nodeChanged(this);
    }

    public void loadChildren(DefaultTreeModel model) {
        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());

        if (getChildCount() > 0) {
            return;
        }

        int c = 0;
        for (ObjectInspector.Slot childSlot : ObjectInspector.getObjectSlots(this.value)) {
            ObjectTreeNode child;
            switch (childSlot.type) {
                case "Dictionary":
                    child = new DictionaryTreeNode(childSlot, this.value + childSlot.offset);
                    break;
                case "Array":
                    child = new ArrayTreeNode(childSlot, API.readLong(this.value + childSlot.offset));
                    break;
                case "Vector":
                    child = new VectorTreeNode(childSlot, this.value + childSlot.offset);
                    break;
                default:
                    child = new ObjectTreeNode(childSlot, this.value + childSlot.offset, false);
                    break;
            }

            if (getChildCount() <= c) {
                model.insertNodeInto(child, this, c++);
            }
            child.update(model);
        }
    }
}

class DictionaryTreeNode extends ObjectTreeNode
{
    public DictionaryTreeNode(ObjectInspector.Slot slot, long address) {
        super(slot, address, true);
    }

    @Override
    public void loadChildren(DefaultTreeModel model) {
        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());
        PairArray dict = PairArray.ofDictionary();
        dict.update(this.value);
        dict.update();

        for (int index = 0; index < dict.getSize(); index++) {
            PairArray.Pair p = dict.get(index);
            ObjectInspector.Slot valueSlot = new ObjectInspector.Slot(p.key, slot.templateType, null, index, 8);
            ObjectTreeNode child = new ObjectTreeNode(valueSlot, p.value, true);
            model.insertNodeInto(child, this, index);
            child.update(model);
        };
    }
}

class ArrayTreeNode extends ObjectTreeNode
{
    public ArrayTreeNode(ObjectInspector.Slot slot, long address) {
        super(slot, address, true);
    }

    @Override
    public void update(DefaultTreeModel model) {
        int denseLength = API.readInt(this.value + 0x28);

        if (denseLength != 0) {
            IntArray arr = IntArray.ofArray(true);
            arr.update(this.value);

            for (int index = 0; index < arr.elements.length; index++) {
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", "int", null, index, 4);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, this.value, false);
                model.insertNodeInto(child, this, index);
                child.update(model);
            }
            while (getChildCount() > arr.getSize()) {
                remove(getChildCount()-1);
            }
        } else {
            PairArray dict = PairArray.ofArray();
            dict.setAutoUpdatable(true);
            dict.setIgnoreEmpty(false);
            dict.update(this.value);

            int c = 0;

            for (int index = 0; index < dict.getSize(); index++) {
                PairArray.Pair p = dict.get(index);
                if (p == null) { break; }
                c++;
                String objectName = ByteUtils.readObjectName(p.value);
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot(p.key, objectName, null, index, 8);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, p.value, true);
                model.insertNodeInto(child, this, index);
            }
            while (getChildCount() > c) {
                remove(getChildCount()-1);
            }
        }
    }

    @Override
    public void loadChildren(DefaultTreeModel model) {

    }
}

class VectorTreeNode extends ObjectTreeNode
{
    public VectorTreeNode(ObjectInspector.Slot slot, long address) {
        super(slot, address, true);
    }

    @Override
    public void loadChildren(DefaultTreeModel model) {
        ObjectInspector.Slot slot = (ObjectInspector.Slot)(this.getUserObject());

        if (slot.templateType.equals("uint") || slot.templateType.equals("int")) {
            IntArray vec = IntArray.ofVector(this.value);
            vec.update();
            for (int index = 0; index < vec.elements.length; index++) {
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", slot.templateType, null, index, 4);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, this.value + slot.offset, false);
                model.insertNodeInto(child, this, index);
                child.update(model);
            }
            while (getChildCount() > vec.getSize()) {
                remove(getChildCount()-1);
            }
        } else {
            ObjArray vec = ObjArray.ofVector();
            vec.update(this.value);
            vec.update();
            for (int index = 0; index < vec.getSize(); index++) {
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", slot.templateType, null, index, 8);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, vec.get(index) & (~7), true);
                model.insertNodeInto(child, this, index);
                child.update(model);
            }

            while (getChildCount() > vec.getSize()) {
                remove(getChildCount()-1);
            }
        }
    }
}
