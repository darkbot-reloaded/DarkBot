package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

public class ObjectTreeNode extends DefaultMutableTreeNode {

    public final Supplier<Long> address;
    private final boolean addressIsValue;

    protected long value = -1;
    protected String strValue;

    ObjectTreeNode(ObjectInspector.Slot slot, Supplier<Long> address, Boolean addressIsValue) {
        super(slot);
        this.address = address;
        this.addressIsValue = addressIsValue;
    }

    @Override
    public boolean isLeaf() {
        ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());
        return value == 0 || slot.size < 8 || slot.type.equals("Number") || slot.type.equals("String");
    }

    @Override
    public String toString() {
        ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());
        return String.format("%03X %-25s  %-25s %s",
                slot.offset, (slot.name.length() >= 25) ? slot.name.substring(25) : slot.name, slot.getType(), strValue);
    }

    public void update(DefaultTreeModel model) {
        long address = this.address.get();
        if (address == 0) {
            return;
        }

        ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());

        boolean changed = value != address;
        if (addressIsValue) {
            value = address;
        } else {
            if (slot.size == 8) {
                value = API.readMemoryLong(address);
            } else if (slot.size == 4) {
                value = API.readMemoryInt(address);
            } else if (slot.size == 1) {
                value = API.readInt(address);
            }

            // Non-leaf means it's an object, which we need to remove atom mask from.
            if (!isLeaf()) value &= ByteUtils.ATOM_MASK;
        }
        if (changed) {
            if (slot.type.equals("Number")) {
                strValue = String.format("%.2f", Double.longBitsToDouble(value));
            } else if (slot.type.equals("String")) {
                strValue = value == 0 ? "null" : String.format("%s", API.readString(value));
            } else if (slot.type.equals("Boolean")) {
                strValue = (value == 1) ? "true" : (value == 0 ? "false" : String.format("Boolean(%d)", this.value));
            } else if (slot.size == 4) {
                strValue = String.format("%d", this.value);
            } else {
                strValue = value == 0 ? "null" : String.format("0x%x", this.value);
            }
        }

        if (children != null) {
            children.forEach(child -> ((ObjectTreeNode) child).update(model));
        }
        if (changed) model.nodeChanged(this);
    }

    public void loadChildren(DefaultTreeModel model) {
        if (getChildCount() > 0) {
            return;
        }

        int c = 0;
        for (ObjectInspector.Slot childSlot : ObjectInspector.getObjectSlots(this.value)) {
            ObjectTreeNode child;
            switch (childSlot.type) {
                case "Dictionary":
                    child = new DictionaryTreeNode(childSlot, () -> this.value + childSlot.offset);
                    break;
                case "Array":
                    child = new ArrayTreeNode(childSlot, () -> API.readLong(this.value + childSlot.offset));
                    break;
                case "Vector":
                    child = new VectorTreeNode(childSlot, () -> this.value + childSlot.offset);
                    break;
                default:
                    child = new ObjectTreeNode(childSlot, () -> this.value + childSlot.offset, false);
                    break;
            }

            if (getChildCount() <= c) {
                model.insertNodeInto(child, this, c++);
            }
            child.update(model);
        }
    }

    private static class DictionaryTreeNode extends ObjectTreeNode {
        public DictionaryTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void loadChildren(DefaultTreeModel model) {
            ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());
            PairArray dict = PairArray.ofDictionary();
            dict.update(this.value);
            dict.update();

            for (int index = 0; index < dict.getSize(); index++) {
                PairArray.Pair p = dict.get(index);
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot(p.key, ByteUtils.readObjectName(p.value), null, index, 8);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> p.value, true);
                model.insertNodeInto(child, this, index);
                child.update(model);
            }
        }
    }

    private static class ArrayTreeNode extends ObjectTreeNode {
        public ArrayTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void update(DefaultTreeModel model) {
            int denseLength = API.readInt(this.value + 0x28);

            if (denseLength != 0) {
                IntArray arr = IntArray.ofArray(true);
                arr.update(this.value);

                for (int index = 0; index < arr.elements.length; index++) {
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", "int", null, index, 4);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> this.value, false);
                    model.insertNodeInto(child, this, index);
                    child.update(model);
                }
                while (getChildCount() > arr.getSize()) {
                    remove(getChildCount() - 1);
                }
            } else {
                PairArray dict = PairArray.ofArray();
                dict.setAutoUpdatable(true);
                dict.setIgnoreEmpty(false);
                dict.update(this.value);

                int c = 0;

                for (int index = 0; index < dict.getSize(); index++) {
                    PairArray.Pair p = dict.get(index);
                    if (p == null) {
                        break;
                    }
                    c++;
                    String objectName = ByteUtils.readObjectName(p.value);
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot(p.key, objectName, null, index, 8);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> p.value, true);
                    model.insertNodeInto(child, this, index);
                }
                while (getChildCount() > c) {
                    remove(getChildCount() - 1);
                }
            }
        }

        @Override
        public void loadChildren(DefaultTreeModel model) {

        }
    }

    private static class VectorTreeNode extends ObjectTreeNode {
        public VectorTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void loadChildren(DefaultTreeModel model) {
            ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());

            if (slot.templateType.equals("uint") || slot.templateType.equals("int")) {
                IntArray vec = IntArray.ofVector(this.value);
                vec.update();
                for (int index = 0; index < vec.elements.length; index++) {
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", slot.templateType, null, index, 4);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> this.value + slot.offset, false);
                    model.insertNodeInto(child, this, index);
                    child.update(model);
                }
                while (getChildCount() > vec.getSize()) {
                    remove(getChildCount() - 1);
                }
            } else {
                ObjArray vec = ObjArray.ofVector();
                vec.update(this.value);
                vec.update();
                for (int index = 0; index < vec.getSize(); index++) {
                    long object = vec.getPtr(index);
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", ByteUtils.readObjectName(object), null, index, 8);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> object & ByteUtils.ATOM_MASK, true);
                    model.insertNodeInto(child, this, index);
                    child.update(model);
                }

                while (getChildCount() > vec.getSize()) {
                    remove(getChildCount() - 1);
                }
            }
        }
    }

}
