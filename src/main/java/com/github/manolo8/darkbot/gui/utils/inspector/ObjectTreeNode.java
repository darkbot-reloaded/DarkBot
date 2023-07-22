package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.objects.swf.IntArray;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Vector;
import java.util.function.Supplier;

import static com.github.manolo8.darkbot.Main.API;

public class ObjectTreeNode extends DefaultMutableTreeNode {

    public final Supplier<Long> address;
    private final boolean addressIsValue;

    protected long value = -1;
    protected String strValue;

    public ObjectTreeNode(ObjectInspector.Slot slot, Supplier<Long> address, boolean addressIsValue) {
        super(slot);
        this.address = address;
        this.addressIsValue = addressIsValue;
    }

    public static ObjectTreeNode root(String name, Supplier<Long> addr) {
        return new ObjectTreeNode(
                new ObjectInspector.Slot(name, "Object", null, 0, 8),
                addr, true);
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

    public void trimChildren(int maxSize) {
        if (children == null) return;
        while (children.size() > maxSize) {
            remove(children.size() - 1);
        }
    }

    public boolean isMemoryWritable() {
        ObjectInspector.Slot slot = (ObjectInspector.Slot) getUserObject();
        return slot.slotType == ObjectInspector.Slot.Type.INT
                || slot.slotType == ObjectInspector.Slot.Type.UINT
                || slot.slotType == ObjectInspector.Slot.Type.DOUBLE
                || slot.slotType == ObjectInspector.Slot.Type.BOOLEAN
                || slot.slotType == ObjectInspector.Slot.Type.OBJECT;
    }

    public void memoryWrite(String text) {
        ObjectInspector.Slot slot = (ObjectInspector.Slot) getUserObject();
        switch (slot.slotType) {
            case INT:
            case UINT:
            case BOOLEAN:
                int i = Integer.parseInt(text);
                API.writeInt(address.get(), i);
                break;
            case DOUBLE:
                double v = Double.parseDouble(text);
                API.writeLong(address.get(), Double.doubleToLongBits(v));
                break;
            case OBJECT:
                Long l = ObjectInspectorUI.tryParse(text);
                if (l != null)
                    API.writeLong(address.get(), l);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void update(InspectorTree tree) {
        long address = this.address.get();
        if (address == 0) {
            return;
        }

        ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());

        long oldValue = value;
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

        boolean changed = oldValue != value;
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

        if (children != null && tree.isExpanded(new TreePath(tree.getModel().getPathToRoot(this)))) {
            children.forEach(child -> ((ObjectTreeNode) child).update(tree));
        }
        if (changed) tree.getModel().nodeChanged(this);
    }

    public void loadChildren(InspectorTree tree) {
        if (children != null) return;
        children = new Vector<>();

        if (value == -1) update(tree);

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
            child.setParent(this);
            children.add(child);
        }

        tree.getModel().nodeStructureChanged(this);
        children.forEach(ch -> ((ObjectTreeNode) ch).update(tree));
    }

    private static class DictionaryTreeNode extends ObjectTreeNode {
        public DictionaryTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void loadChildren(InspectorTree tree) {
            PairArray dict = PairArray.ofDictionary();
            dict.update(this.value);
            dict.update();

            for (int index = 0; index < dict.getSize(); index++) {
                PairArray.Pair p = dict.get(index);
                ObjectInspector.Slot valueSlot = new ObjectInspector.Slot(p.key, ByteUtils.readObjectName(p.value), null, index, 8);
                ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> p.value, true);
                tree.getModel().insertNodeInto(child, this, index);
                child.update(tree);
            }
        }
    }

    private static class ArrayTreeNode extends ObjectTreeNode {
        public ArrayTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void update(InspectorTree tree) {
            int denseLength = API.readInt(this.value + 0x28);

            if (denseLength != 0) {
                IntArray arr = IntArray.ofArray(true);
                arr.update(this.value);

                for (int index = 0; index < arr.elements.length; index++) {
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", "int", null, index, 4);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> this.value, false);
                    tree.getModel().insertNodeInto(child, this, index);
                    child.update(tree);
                }
                trimChildren(arr.getSize());
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
                    tree.getModel().insertNodeInto(child, this, index);
                }
                trimChildren(c);
            }
        }

        @Override
        public void loadChildren(InspectorTree model) {
            // Children are populated in update method for arrays
        }
    }

    private static class VectorTreeNode extends ObjectTreeNode {
        public VectorTreeNode(ObjectInspector.Slot slot, Supplier<Long> address) {
            super(slot, address, false);
        }

        @Override
        public void loadChildren(InspectorTree tree) {
            ObjectInspector.Slot slot = (ObjectInspector.Slot) (this.getUserObject());

            if (slot.templateType.equals("uint") || slot.templateType.equals("int")) {
                IntArray vec = IntArray.ofVector(this.value);
                vec.update();
                for (int index = 0; index < vec.elements.length; index++) {
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", slot.templateType, null, index, 4);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> this.value + slot.offset, false);
                    tree.getModel().insertNodeInto(child, this, index);
                    child.update(tree);
                }
                trimChildren(vec.getSize());
            } else {
                ObjArray vec = ObjArray.ofVector();
                vec.update(this.value);
                vec.update();
                for (int index = 0; index < vec.getSize(); index++) {
                    long object = vec.getPtr(index);
                    ObjectInspector.Slot valueSlot = new ObjectInspector.Slot("", ByteUtils.readObjectName(object), null, index, 8);
                    ObjectTreeNode child = new ObjectTreeNode(valueSlot, () -> object & ByteUtils.ATOM_MASK, true);
                    tree.getModel().insertNodeInto(child, this, index);
                    child.update(tree);
                }
                trimChildren(vec.getSize());
            }
        }
    }

}
