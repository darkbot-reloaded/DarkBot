package com.github.manolo8.darkbot.core.itf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import org.jetbrains.annotations.Nullable;

public interface NativeUpdatable {

    long getAddress();

    default void update() {}
    default void update(long address) {}

    default int modifyOffset(int offset) {
        return offset;
    }

    default int readInt(int offset) {
        return Main.API.readInt(getAddress() + modifyOffset(offset));
    }

    default int readInt(int... offsets) {
        return Main.API.readInt(getAddress(), modifyOffset(offsets));
    }

    // it is a native class which holds min[32], max[36] & value[40] members
    default int readIntHolder(int offset) {
        return Main.API.readInt(readLong(offset) + 40);
    }

    default long readLong(int offset) {
        return Main.API.readLong(getAddress() + modifyOffset(offset));
    }

    default long readLong(int... offsets) {
        return Main.API.readLong(getAddress(), modifyOffset(offsets));
    }

    /**
     * @see ByteUtils#ATOM_MASK
     */
    default long readAtom(int offset) {
        return readLong(offset) & ByteUtils.ATOM_MASK;
    }

    default long getClassClosure() {
        return ByteUtils.getClassClosure(getAddress());
    }

    default double readDouble(int offset) {
        return Main.API.readDouble(getAddress() + modifyOffset(offset));
    }

    default double readDouble(int... offsets) {
        return Main.API.readDouble(getAddress(), modifyOffset(offsets));
    }

    default boolean readBoolean(int offset) {
        return Main.API.readBoolean(getAddress() + modifyOffset(offset));
    }

    default boolean readBoolean(int... offsets) {
        return Main.API.readBoolean(getAddress(), modifyOffset(offsets));
    }

    default @Nullable String readString(int... offsets) {
        return readString(null, offsets);
    }

    default String readString(String fallback, int... offsets) {
        return Main.API.readString(getAddress(), fallback, modifyOffset(offsets));
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
