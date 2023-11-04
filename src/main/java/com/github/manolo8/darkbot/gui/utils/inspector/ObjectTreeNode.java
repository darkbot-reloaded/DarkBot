package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashMap;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;
import eu.darkbot.util.Popups;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.LongSupplier;

import static com.github.manolo8.darkbot.Main.API;

public class ObjectTreeNode extends DefaultMutableTreeNode {
    private static int nameLastLength = 25, nameCurrentLength = 25;
    private static int typeLastLength = 25, typeCurrentLength = 25;

    @Getter protected String text;
    @Getter protected ObjectInspector.Slot slot;

    protected LongSupplier address;
    protected boolean staticAddress, readChildren;
    protected long value = Long.MIN_VALUE;

    private double percentChange;
    private long lastChange;
    private int slotsCount;

    protected ObjectTreeNode(ObjectInspector.Slot slot, LongSupplier address, boolean staticAddress) {
        this.slot = slot;
        this.address = address;
        this.staticAddress = staticAddress;

        //todo should keep slot count?
        if (slot.size == 8 && slot.slotType != ObjectInspector.Slot.Type.DOUBLE
                && slot.slotType != ObjectInspector.Slot.Type.STRING) {
            long adr = staticAddress ? API.readLong(address.getAsLong()) : address.getAsLong();
            List<ObjectInspector.Slot> objectSlots = ObjectInspector.getObjectSlots(adr);
            slotsCount = objectSlots.size();
        }
    }

    public static boolean maxTextLengthChanged() {
        boolean changed = nameLastLength != nameCurrentLength || typeLastLength != typeCurrentLength;
        nameLastLength = nameCurrentLength;
        typeLastLength = typeCurrentLength;

        typeCurrentLength = 25;
        nameCurrentLength = 25;
        return changed;
    }

    public static ObjectTreeNode root(String type, LongSupplier address) {
        return new ObjectTreeNode(new ObjectInspector.Slot("-", type, null, 0, 8), address, false);
    }

    private static String extractType(String slotName) {
        int i = slotName.indexOf(".<");
        return i == -1 ? slotName : slotName.substring(0, i);
    }

    private static String extractTemplateType(String slotName) {
        int i = slotName.indexOf(".<");
        if (i == -1) return null;

        int j = slotName.lastIndexOf("::");
        return slotName.substring((j == -1 ? i : j) + 2, slotName.length() - 1);
    }

    private static ObjectTreeNode createNode(LongSupplier address, ObjectInspector.Slot slot, boolean staticAddress) {
        long object = staticAddress ? API.readMemoryPtr(address.getAsLong()) : address.getAsLong();
        if (object == 0) new ObjectTreeNode(slot, address, staticAddress);

        boolean hasHashMap = FlashMap.hasHashMap(object);

        //todo move & fix in ObjectInspector#getObjectSlots method
        if (slot.slotType == ObjectInspector.Slot.Type.ARRAY
                || slot.slotType == ObjectInspector.Slot.Type.VECTOR
                || slot.slotType == ObjectInspector.Slot.Type.DICTIONARY
                || slot.slotType == ObjectInspector.Slot.Type.PLAIN_OBJECT
                || slot.slotType == ObjectInspector.Slot.Type.OBJECT) {

            String name = ByteUtils.readObjectNameDirect(object);
            if (!name.equals("ERROR")) {
                String type = extractType(name);
                String templateType = extractTemplateType(name);

                slot.setType(type);
                if (templateType != null)
                    slot.setTemplateType(templateType);
                slot.slotType = ObjectInspector.Slot.Type.of(slot);
            }
        }

        switch (slot.slotType) {
            case ARRAY:
                boolean denseUsed = API.readInt(object + 52) != 0;
                return new CollectionNode(slot, address, staticAddress, hasHashMap && !denseUsed, false);
            case DICTIONARY:
                return new CollectionNode(slot, address, staticAddress, true, false);
            case VECTOR:
                return new CollectionNode(slot, address, staticAddress, false, true);
            case PLAIN_OBJECT:
                if (hasHashMap)
                    return new CollectionNode(slot, address, staticAddress, true, false);
            default:
                return new ObjectTreeNode(slot, address, staticAddress);
        }
    }

    @Override
    public boolean isLeaf() {
        return address.getAsLong() <= 0xFFFF || (staticAddress && value < 0xFFFF)
                || slot.size < 8
                || slot.slotType == ObjectInspector.Slot.Type.DOUBLE
                || slot.slotType == ObjectInspector.Slot.Type.STRING;
    }

    public boolean isMemoryWritable() {
        return address.getAsLong() > 0xFFFF &&
                (slot.slotType == ObjectInspector.Slot.Type.INT
                || slot.slotType == ObjectInspector.Slot.Type.UINT
                || slot.slotType == ObjectInspector.Slot.Type.DOUBLE
                || slot.slotType == ObjectInspector.Slot.Type.BOOLEAN
                || slot.slotType == ObjectInspector.Slot.Type.OBJECT);
    }

    public void memoryWrite(String text) {
        text = text.trim();

        switch (slot.slotType) {
            case BOOLEAN:
                Boolean result = null;

                if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("1")) result = true;
                if (text.equalsIgnoreCase("false") || text.equalsIgnoreCase("0")) result = false;

                if (result != null) {
                    API.writeInt(address.getAsLong(), result ? 1 : 0);
                    return;
                }
                break;
            case INT:
            case UINT:
                try {
                    API.writeInt(address.getAsLong(), Integer.parseInt(text));
                    return;
                } catch (NumberFormatException ignore) {
                }
                break;
            case DOUBLE:
                try {
                    API.writeLong(address.getAsLong(), Double.doubleToLongBits(Double.parseDouble(text)));
                    return;
                } catch (NumberFormatException ignore) {
                }
                break;
            case OBJECT:
                Long l = ObjectInspectorUI.parseAddress(text);
                if (l != null) {
                    API.writeLong(address.getAsLong(), l);
                    return;
                }
                break;
        }
        String name = slot.slotType.name().toLowerCase(Locale.ROOT);
        Popups.of("Invalid " + name + " value",
                "Expected a " + name + " value, but got '" + text + "'", JOptionPane.ERROR_MESSAGE).showAsync();
    }

    public @Nullable Color getBackgroundColor(Color bg) {
        return percentChange > 0 ? UIUtils.darker(bg, 1 - percentChange * 0.25) : null;
    }

    public void update(InspectorTree tree) {
        long address = this.address.getAsLong();
        long oldValue = value;
        if (staticAddress) {
            if (slot.size == 8) {
                value = API.readMemoryPtr(address);
            } else if (slot.size == 4) {
                value = API.readMemoryInt(address);
            } else if (slot.size == 1) {
                value = API.readInt(address);
            }
        } else value = address;

        if (oldValue != value) {
            if (slot.slotType == ObjectInspector.Slot.Type.DOUBLE) {
                text = String.format("%.3f", Double.longBitsToDouble(value));
            } else if (slot.slotType == ObjectInspector.Slot.Type.STRING) {
                text = value == 0 ? "null" : String.format("%s", API.readStringDirect(value));
            } else if (slot.slotType == ObjectInspector.Slot.Type.BOOLEAN) {
                text = (value == 1) ? "true" : (value == 0 ? "false" : String.format("Boolean(%d)", this.value));
            } else if (slot.size == 4) {
                text = String.format("%d", this.value);
            } else {
                text = value == 0 ? "null" : String.format("0x%x", this.value);
            }

            // do not change color on init
            if (lastChange == 0) {
                lastChange = 1;
            } else {
                lastChange = System.currentTimeMillis();
                percentChange = 1;
            }
            tree.getModel().nodeChanged(this);
        } else {
            double percent = Math.max(0, (double) (lastChange + 1000 - System.currentTimeMillis()) / 1000);
            if (this.percentChange != percent) {
                this.percentChange = percent;
                tree.getModel().nodeChanged(this);
            }
        }

        updateChildren(tree, readChildren);

        if (children != null) {
            for (TreeNode child : children) {
                ObjectTreeNode node = (ObjectTreeNode) child;
                node.update(tree);
            }
        }

        nameCurrentLength = Math.max(nameCurrentLength, slot.name.length());
        typeCurrentLength = Math.max(typeCurrentLength, slot.getType().length());
    }

    protected void updateChildren(InspectorTree tree, boolean update) {
    }

    public void loadChildren(InspectorTree tree) {
        update(tree);
        for (ObjectInspector.Slot slot : ObjectInspector.getObjectSlots(this.value)) {
            ObjectTreeNode child = createNode(() -> this.value + slot.offset, slot, true);
            add(child);
        }

        if (getChildCount() > 0) {
            tree.getModel().nodeStructureChanged(this);

            for (TreeNode child : children) {
                ObjectTreeNode node = (ObjectTreeNode) child;
                node.update(tree);
            }
        }
    }

    public void unloadChildren(InspectorTree tree) {
        if (children != null) {
            remove(tree, new ArrayList<>(children));
        }
    }

    protected void remove(InspectorTree tree, List<TreeNode> toRemove) {
        for (TreeNode node : toRemove) {
            tree.getModel().removeNodeFromParent((MutableTreeNode) node);
        }
    }

    protected void trimChildren(InspectorTree tree, int maxSize) {
        if (children == null || maxSize >= children.size()) return;

        List<TreeNode> list = new ArrayList<>();
        for (int i = maxSize; i < children.size(); i++) {
            list.add(children.get(i));
        }
        remove(tree, list);
    }

    @Override
    public String toString() {
        int nameLength = nameLastLength;
        int typeLength = typeLastLength;
        String format = (slot.isInArray ? "%03d" : "%03X") + " %-" + nameLength + "s  %-" + typeLength + "s %s";

        return String.format(format, slot.offset,
                (slot.name.length() > nameLength) ? (slot.name.substring(0, nameLength - 3) + "...") : slot.name,
                (slot.getType().length() > typeLength) ? (slot.getType().substring(0, typeLength - 3) + "...") : slot.getType(),
                slot.isInArray ? slot.valueText : text)
                + (slotsCount > 0 ? " [" + slotsCount + "]" : "");
    }

    private static class CollectionNode extends ObjectTreeNode {
        private final FlashList<?> list;
        private final FlashMap<?, ?> map;

        private int oldSize;

        protected CollectionNode(ObjectInspector.Slot slot, LongSupplier address, boolean staticAddress,
                                 boolean isMap, boolean isVector) {
            super(slot, address, staticAddress);
            if (isMap) {
                this.map = FlashMap.ofUnknown().makeThreadSafe();
                this.list = null;
            } else {
                this.map = null;
                this.list = (isVector ? FlashList.ofVectorUnknown() : FlashList.ofArrayUnknown()).makeThreadSafe();
            }
        }

        @Override
        public void loadChildren(InspectorTree tree) {
            readChildren = true;
            update(tree);
        }

        @Override
        public void unloadChildren(InspectorTree tree) {
            readChildren = false;
            super.unloadChildren(tree);
        }

        @Override
        protected void updateChildren(InspectorTree tree, boolean update) {
            int size;
            if (map != null) {
                map.update(value);
                map.update(); // read size of map;
                size = map.size();
            } else {
                list.update(value);
                list.update();
                size = list.size();
            }

            if (oldSize != size) {
                tree.getModel().nodeChanged(this);
                oldSize = size;
            }

            if (!update) return;
            List<Integer> insertedIndexes = new ArrayList<>();

            int i = 0;
            if (map != null) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (updateChild(i, entry.getValue(), entry.getKey().toString(), tree.getModel()))
                        insertedIndexes.add(i);
                    i++;
                }
            } else {
                for (; i < list.size(); i++) {
                    if (updateChild(i, list.get(i), "", tree.getModel()))
                        insertedIndexes.add(i);
                }
            }

            if (!insertedIndexes.isEmpty())
                tree.getModel().nodesWereInserted(this, insertedIndexes.stream().mapToInt(Integer::intValue).toArray());
            trimChildren(tree, i);
        }

        // true -> inserted
        private boolean updateChild(int i, Object o, String name, DefaultTreeModel treeModel) {
            ObjectInspector.Slot valueSlot = createSlot(name, o, i + 1);
            if (getChildCount() > i) {
                ObjectTreeNode child = (ObjectTreeNode) getChildAt(i);

                child.address = () -> o instanceof Number ? ((Number) o).longValue() : 0;
                if (!child.slot.equals(valueSlot)) {
                    child.slot = valueSlot;
                    treeModel.nodeChanged(child);
                }
            } else {
                ObjectTreeNode child = createNode(() -> o instanceof Number ? ((Number) o).longValue() : 0L, valueSlot, false);
                add(child);
                return true;
            }
            return false;
        }

        private ObjectInspector.Slot createSlot(String name, Object value, int offset) {
            String type = "";
            String templateType = null;

            if (value instanceof Long) {
                String s = ByteUtils.readObjectNameDirect((Long) value);
                if (!s.equals("ERROR")) {
                    type = extractType(s);
                    templateType = extractTemplateType(s);
                }
            } else {
                if (this.slot.templateType != null && !this.slot.templateType.equals("ERROR")) {
                    type = this.slot.templateType;
                } else {
                    type = value.getClass().getSimpleName();
                }
            }

            int size = value instanceof Integer || value instanceof Boolean ? 4 : 8;
            ObjectInspector.Slot slot = new ObjectInspector.Slot(name, type, templateType, offset, size);

            slot.isInArray = true;
            slot.valueText = value instanceof Long ? String.format("0x%x", value) : value.toString();
            return slot;
        }

        @Override
        public String toString() {
            if (value <= 0xFFFF) return super.toString();
            return super.toString() + " " + (map == null ? list : map);
        }
    }
}
