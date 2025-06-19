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

    // Reads 'BindableInt' holder value
    default int readBindableInt(int o1) {
        return Main.API.readBindableInt(getAddress(), modifyOffset(o1));
    }

    default int readBindableInt(int o1, int o2) {
        return Main.API.readBindableInt(getAddress(), modifyOffset(o1), o2);
    }

    default int readBindableInt(int o1, int o2, int o3) {
        return Main.API.readBindableInt(getAddress(), modifyOffset(o1), o2, o3);
    }

    // Reads 'BindableString' holder value
    default String readBindableString(String fallback, int offset) {
        return readString(fallback, offset, 40);
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

    /**
     * @return the string or {@link com.github.manolo8.darkbot.core.MemoryAPI#FALLBACK_STRING} if return value is null
     */
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
}
