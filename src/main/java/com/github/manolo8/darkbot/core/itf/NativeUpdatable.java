package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

public interface NativeUpdatable {

    long getAddress();

    default void update() {}
    default void update(long address) {}

    default int modifyOffset(int offset) {
        return offset;
    }

    default int readInt(int o1) {
        return Main.API.readInt(getAddress(), modifyOffset(o1));
    }

    default int readInt(int o1, int o2) {
        return Main.API.readInt(getAddress(), modifyOffset(o1), o2);
    }

    default int readInt(int o1, int o2, int o3) {
        return Main.API.readInt(getAddress(), modifyOffset(o1), o2, o3);
    }

    default int readInt(int o1, int o2, int o3, int o4) {
        return Main.API.readInt(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default int readInt(int... offsets) {
        return Main.API.readInt(getAddress(), modifyOffset(offsets));
    }

    // it is a native class which holds min[32], max[36] & value[40] members
    default int readIntHolder(int offset) {
        return readInt(offset, 40);
    }

    default long readLong(int o1) {
        return Main.API.readLong(getAddress(), modifyOffset(o1));
    }

    default long readLong(int o1, int o2) {
        return Main.API.readLong(getAddress(), modifyOffset(o1), o2);
    }

    default long readLong(int o1, int o2, int o3) {
        return Main.API.readLong(getAddress(), modifyOffset(o1), o2, o3);
    }

    default long readLong(int o1, int o2, int o3, int o4) {
        return Main.API.readLong(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default long readLong(int o1, int o2, int o3, int o4, int o5) {
        return Main.API.readLong(getAddress(), modifyOffset(o1), o2, o3, o4, o5);
    }

    default long readLong(int... offsets) {
        return Main.API.readLong(getAddress(), modifyOffset(offsets));
    }

    /**
     * @see ByteUtils#ATOM_MASK
     */
    default long readAtom(int o1) {
        return Main.API.readAtom(getAddress(), modifyOffset(o1));
    }

    default long readAtom(int o1, int o2) {
        return Main.API.readAtom(getAddress(), modifyOffset(o1), o2);
    }

    default long readAtom(int o1, int o2, int o3) {
        return Main.API.readAtom(getAddress(), modifyOffset(o1), o2, o3);
    }

    default long readAtom(int o1, int o2, int o3, int o4) {
        return Main.API.readAtom(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default long getClassClosure() {
        return ByteUtils.getClassClosure(getAddress());
    }

    default double readDouble(int o1) {
        return Main.API.readDouble(getAddress(), modifyOffset(o1));
    }

    default double readDouble(int o1, int o2) {
        return Main.API.readDouble(getAddress(), modifyOffset(o1), o2);
    }

    default double readDouble(int o1, int o2, int o3) {
        return Main.API.readDouble(getAddress(), modifyOffset(o1), o2, o3);
    }

    default double readDouble(int o1, int o2, int o3, int o4) {
        return Main.API.readDouble(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default double readDouble(int... offsets) {
        return Main.API.readDouble(getAddress(), modifyOffset(offsets));
    }

    default boolean readBoolean(int o1) {
        return Main.API.readBoolean(getAddress(), modifyOffset(o1));
    }

    default boolean readBoolean(int o1, int o2) {
        return Main.API.readBoolean(getAddress(), modifyOffset(o1), o2);
    }

    default boolean readBoolean(int o1, int o2, int o3) {
        return Main.API.readBoolean(getAddress(), modifyOffset(o1), o2, o3);
    }

    default boolean readBoolean(int o1, int o2, int o3, int o4) {
        return Main.API.readBoolean(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default boolean readBoolean(int... offsets) {
        return Main.API.readBoolean(getAddress(), modifyOffset(offsets));
    }

    /**
     * @return the string or {@link eu.darkbot.api.managers.MemoryAPI#FALLBACK_STRING} if return value is null
     * */
    default String readString(int o1) {
        return Main.API.readString(getAddress(), modifyOffset(o1));
    }

    default String readString(int o1, int o2) {
        return Main.API.readString(getAddress(), modifyOffset(o1), o2);
    }

    default String readString(int o1, int o2, int o3) {
        return Main.API.readString(getAddress(), modifyOffset(o1), o2, o3);
    }

    default String readString(int o1, int o2, int o3, int o4) {
        return Main.API.readString(getAddress(), modifyOffset(o1), o2, o3, o4);
    }

    default String readString(int o1, int o2, int o3, int o4, int o5) {
        return Main.API.readString(getAddress(), modifyOffset(o1), o2, o3, o4, o5);
    }

    default String readString(String fallback, int o1) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(o1));
    }

    default String readString(String fallback, int o1, int o2) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(o1), o2);

    }

    default String readString(String fallback, int o1, int o2, int o3) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(o1), o2, o3);
    }
    default String readString(String fallback, int o1, int o2, int o3, int o4) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(o1), o2, o3, o4);
    }

    default String readString(String fallback, int o1, int o2, int o3, int o4, int o5) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(o1), o2, o3, o4, o5);
    }

    default void replaceInt(int oldValue, int newValue, int offset) {
        Main.API.replaceInt(getAddress() + modifyOffset(offset), oldValue, newValue);
    }

    private int[] modifyOffset(int... offsets) {
        if (offsets.length > 0) {
            offsets[0] = modifyOffset(offsets[0]);
        }
        return offsets;
    }
}
