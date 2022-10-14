package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.util.function.LongFunction;

public enum AtomKind {
    UNUSED(),
    OBJECT(Long.class, atom -> atom & ByteUtils.ATOM_MASK),
    STRING(String.class, atom -> Main.API.readString(atom & ByteUtils.ATOM_MASK)),
    NAMESPACE(), // ?
    SPECIAL(Float.class),// prob not supported
    BOOLEAN(Boolean.class, atom -> atom == 0xD),
    INTEGER(Integer.class, atom -> (int) ((atom & ByteUtils.ATOM_MASK) >> 3)),
    DOUBLE(Double.class, atom -> Double.longBitsToDouble(atom & ByteUtils.ATOM_MASK));// probably should be Main.API.readDouble(atom & MASK)

    private static final AtomKind[] VALUES = values();

    private final Class<?> javaType;
    private final LongFunction<Object> readAtom;

    AtomKind() {
        this(null);
    }

    AtomKind(Class<?> javaType) {
        this(javaType, null);
    }

    AtomKind(Class<?> javaType, LongFunction<Object> readAtom) {
        this.javaType = javaType;
        this.readAtom = readAtom;
    }

    public <T> T read(long atom) {
        return (T) readAtom.apply(atom);
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