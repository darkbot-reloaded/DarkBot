package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.function.BiFunction;
import java.util.function.LongFunction;

public enum AtomKind {
    UNUSED(),
    OBJECT(Long.class, atom -> atom & ByteUtils.ATOM_MASK, ByteUtils::getLong),
    STRING(String.class, atom -> Main.API.readString(atom & ByteUtils.ATOM_MASK),
            (b, i) -> Main.API.readString(ByteUtils.getLong(b, i))),
    NAMESPACE(), // ?
    SPECIAL(Float.class),// prob not supported
    BOOLEAN(Boolean.class, atom -> atom == 0xD, (b, i) -> b[i] == 1),
    INTEGER(Integer.class, atom -> (int) ((atom & ByteUtils.ATOM_MASK) >> 3), ByteUtils::getInt),
    DOUBLE(Double.class, atom -> Double.longBitsToDouble(atom & ByteUtils.ATOM_MASK), ByteUtils::getDouble);

    private static final AtomKind[] VALUES = values();

    private final Class<?> javaType;
    private final LongFunction<Object> readAtom;
    private final BiFunction<byte[], Integer, Object> readBuffer;

    AtomKind() {
        this(null);
    }

    AtomKind(Class<?> javaType) {
        this(javaType, null, null);
    }

    AtomKind(Class<?> javaType, LongFunction<Object> readAtom, BiFunction<byte[], Integer, Object> readBuffer) {
        this.javaType = javaType;
        this.readAtom = readAtom;
        this.readBuffer = readBuffer;
    }

    @SuppressWarnings("unchecked")
    public <T> T readAtom(long atom, boolean threadSafe) {
        if (threadSafe && this == STRING)
            return (T) Main.API.readStringDirect(atom & ByteUtils.ATOM_MASK);
        return (T) readAtom.apply(atom);
    }

    @SuppressWarnings("unchecked")
    public <T> T readBuffer(byte[] buffer, int offset) {
        return (T) readBuffer.apply(buffer, offset);
    }

    public boolean isNotSupported() {
        return readAtom == null;
    }

    public static AtomKind of(long atom) {
        int kind = (int) (atom & ByteUtils.ATOM_KIND);
        if (kind >= VALUES.length) return UNUSED;

        return VALUES[kind];
    }

    public static AtomKind of(Class<?> type) {
        if (Updatable.class.isAssignableFrom(type)) return OBJECT;
        for (AtomKind kind : VALUES) {
            if (kind.javaType == type)
                return kind;
        }
        return UNUSED;
    }

    public static boolean isNullAtom(long atom) {
        return atom <= 4;
    }

    public static boolean isString(long atom) {
        return AtomKind.of(atom) == STRING;
    }
}