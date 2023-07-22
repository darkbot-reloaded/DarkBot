package com.github.manolo8.darkbot.utils.debug;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.OSUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class ObjectInspector {

    public enum TraitKind {
        SLOT(0x00),
        METHOD(0x01),
        GETTER(0x02),
        SETTER(0x03),
        CLASS(0x04),
        PAD(0x07),
        CONST(0x06),
        COUNT(0x07);

        private final int kind;

        TraitKind(int kind) {
            this.kind = kind;
        }
    };

    public static class Trait {
        int name;
        TraitKind kind;
        int typeId = 0;
        int id = 0;
        int temp;
    }

    public static class Slot {

        public String name;
        public String type;
        public String templateType;
        public long offset;
        public long size;
        public Type slotType;

        long getOffset() {
            return offset;
        }

        public String getType() {
            String type = this.type;
            if (templateType != null && !templateType.equals("ERROR"))
                type += "<" + templateType + ">";

            return type;
        }

        public String toString() {
            return String.format("%03X  -  %s  %s", offset, name, type);
        }

        public Slot(String name, String type, String templateType, long offset, long size) {
            this.name = name;
            this.type = type;
            this.templateType = templateType;
            this.offset = offset;
            this.size = size;

            this.slotType = Type.of(this);
        }

        public enum Type {
            INT("int"),
            UINT("uint"),
            BOOLEAN("Boolean"),
            STRING("String"),
            DOUBLE("Number"),
            ARRAY("Array"),
            VECTOR("Vector"),
            DICTIONARY("Dictionary"),
            OBJECT(null);

            private final String typeName;

            Type(String typeName) {
                this.typeName = typeName;
            }

            static Type of(Slot slot) {
                for (Type value : values()) {
                    if (slot.type.equals(value.typeName))
                        return value;
                }

                return slot.size >= 8 ? OBJECT : INT;
            }
        }
    }

    private static class TraitsPtr {
        long address;
        byte[] bs = new byte[5];

        TraitsPtr(long address) {
            this.address = address;
        }

        byte readByte() {
            bs[0] = 0;
            API.readMemory(this.address++, bs, 1);
            return bs[0];
        }

        int readU32() {
            Arrays.fill(bs, (byte) 0);
            API.readMemory(this.address, bs,5);

            int result = bs[0];
            if ((result & 0x00000080) == 0) {
                this.address++;
                return result;
            }
            result = (result & 0x0000007f) | bs[1]<<7;
            if ((result & 0x00004000) == 0) {
                this.address += 2;
                return result;
            }
            result = (result & 0x00003fff) | (int)bs[2]<<14;
            if ((result & 0x00200000) == 0) {
                this.address += 3;
                return result;
            }
            result = (result & 0x001fffff) | (int)bs[3]<<21;
            if ((result & 0x10000000) == 0) {
                this.address+=4;
                return result;
            }
            result = (result & 0x0fffffff) | (int)bs[4]<<28;
            this.address += 5;
            return result;
        }
    }

    private static List<Trait> parseTraitsInternal(long address) {
        long base = API.readMemoryLong(address + 0x10);

        List<Trait> traits = new ArrayList<Trait>();

        long traitsPos = API.readLong(address + 0xb0);
        int posType   = API.readInt(address + 0xf5) & 0xff;

        TraitsPtr traitsPtr = new TraitsPtr(traitsPos);

        if (posType == 0)
        {
            /* auto qname = */traitsPtr.readU32();
            /* auto sname = */traitsPtr.readU32();

            byte flags = traitsPtr.readByte();

            if ((flags & 8) != 0) {
                traitsPtr.readU32(); // Skip
            }

            int interfaceCount = traitsPtr.readU32();
            for (int i = 0; i < interfaceCount; i++) {
                traitsPtr.readU32(); // Skip
            }
        }

        traitsPtr.readU32(); // Skip iinit

        int traitCount = traitsPtr.readU32();

        for (int i = 0; i < traitCount; i++) {
            Trait trait = new Trait();

            int name = traitsPtr.readU32();
            byte tag = traitsPtr.readByte();

            TraitKind kind = TraitKind.values()[((int)tag & 0xf)];

            trait.name = name;
            trait.kind = kind;

            switch(kind) {
                case SLOT:
                case CONST:
                {
                    /* int slot_id = */ traitsPtr.readU32();
                    int typeName = traitsPtr.readU32();
                    int vindex = traitsPtr.readU32(); // references one of the tables in the constant pool, depending on the value of vkind

                    trait.id = vindex;
                    trait.typeId = typeName;

                    if (vindex != 0) {
                        traitsPtr.readByte(); // vkind, ignored by the avm
                    }
                    break;
                }
                case CLASS:
                {
                    /* int slot_id = */ traitsPtr.readU32();
                    int class_index = traitsPtr.readU32(); //  is an index that points into the class array of the abcFile entry

                    trait.id = class_index;
                    break;
                }
                case METHOD:
                case GETTER:
                case SETTER:
                {
                    // The disp_id field is a compiler assigned integer that is used by the AVM2 to optimize the resolution of
                    // virtual function calls. An overridden method must have the same disp_id as that of the method in the
                    // base class. A value of zero disables this optimization.
                    /* int disp_id      = */ traitsPtr.readU32();
                    int method_index = traitsPtr.readU32(); // is an index that points into the method array of the abcFile e

                    trait.id = method_index;
                    trait.temp = name;
                    break;
                }
                default:
                    break;
            }

            // ATTR_metadata
            if ((tag & 0x40) != 0) {
                int metadata_count  = traitsPtr.readU32();
                for (int j = 0; j < metadata_count; j++) {
                    /* int index = */ traitsPtr.readU32();
                }
            }
            traits.add(trait);
        }
        return traits;
    }

    public static List<Slot> getObjectSlots(long address) {
        address = address & ByteUtils.ATOM_MASK;
        // obj -> vtable -> vtable init -> vtable scope -> abc env -> const pool
        long pool = API.readLong(address, 0x10, 0x10, 0x18, 0x10, 0x8);
        long traits = API.readLong(address, 0x10, 0x28);
        return getTraitsBinding(traits, pool);
    }

    private static List<Slot> getTraitsBinding (long traits, long pool) {
        List<Slot> result = new ArrayList<Slot>();

        long offset = API.readInt(traits + 0xea) & 0xffff;
        long base = API.readLong(traits + 0x10);

        if (offset == 0 && base != 0) {
            int hashTableOffset = API.readInt(base + 0xec);
            int totalSize = API.readInt(base + 0xf0);

            offset = (hashTableOffset != 0) ? hashTableOffset : totalSize;
            result = getTraitsBinding(base, pool);
        }

        int precompMnSize = API.readInt(pool + 0x98 + (OSUtil.isWindows() ? -0x18 : 0));
        long precompMn = API.readLong(pool + 0xe8 + (OSUtil.isWindows() ? -0x20 : 0));

        int slot32Count = 0;
        int slotPointerCount = 0;
        int slot64Count = 0;

        List<Trait> parsed_traits = parseTraitsInternal(traits);

        for (Trait trait : parsed_traits) {
            if ((trait.kind != TraitKind.SLOT && trait.kind != TraitKind.CONST) || trait.name > precompMnSize) {
                continue;
            }

            long typeAddr = API.readLong(precompMn + (trait.typeId + 1) * 0x18);

            String typeName = API.readString(typeAddr);

            if (typeName.equals("Boolean") || typeName.equals("int") || typeName.equals("uint")) {
                slot32Count++;
            } else if (typeName.equals("Number")) {
                slot64Count++;
            } else {
                slotPointerCount++;
            }
        }

        long next32 = offset;
        long nextPointer = offset + ((slot32Count * 4 + 4) & (~7));
        long next64 = nextPointer + slotPointerCount * 8;

        for (Trait trait : parsed_traits) {
            if ((trait.kind != TraitKind.SLOT && trait.kind != TraitKind.CONST) || trait.name > precompMnSize) {
                continue;
            }
            long nameAddr = API.readLong(precompMn + (trait.name + 1) * 0x18);
            long typeAddr = API.readLong(precompMn + (trait.typeId + 1) * 0x18);
            String typeName = API.readString(typeAddr);
            String templateType = null;

            long slotSize;

            // Get template type
            if (typeName.equals("Vector") || typeName.equals("Dictionary")) {
                int vectorTypeId = API.readInt(precompMn + (trait.typeId + 1) * 0x18 + 0x14);
                templateType = API.readString(precompMn + (vectorTypeId + 1) * 0x18, 0);
            }

            if (typeName.equals("Boolean") || typeName.equals("int") || typeName.equals("uint")) {
                offset = next32;
                next32 += 4;
                slotSize = 4;
            } else if (typeName.equals("Number")) {
                offset = next64;
                next64 += 8;
                slotSize = 8;
            } else {
                offset = nextPointer;
                nextPointer += 8;
                slotSize = 8;
            }
            result.add(new Slot(API.readString(nameAddr), typeName, templateType, offset, slotSize));
        }

        result.sort(Comparator.comparing(Slot::getOffset));
        return result;
    }
}
