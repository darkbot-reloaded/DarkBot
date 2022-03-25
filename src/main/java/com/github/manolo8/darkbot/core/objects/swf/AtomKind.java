package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

public enum AtomKind {
    UNUSED(null),
    OBJECT(Long.class),
    STRING(String.class) {
        @Override
        public String read(long atom) {
            return Main.API.readString((Long) super.read(atom));
        }
    },
    NAMESPACE(null), // ?
    SPECIAL(Float.class),// prob not supported
    BOOLEAN(Boolean.class) {
        @Override
        public Boolean read(long atom) {
            return atom == 0x0D; // 0x0D == atom true, 0x05 == atom false;
        }
    },
    INTEGER(Integer.class) {
        @Override
        public Integer read(long atom) {
            return (int) ((atom & ByteUtils.ATOM_MASK) >> 3);
        }
    },
    DOUBLE(Double.class) {
        @Override
        public Double read(long atom) {
            return Double.longBitsToDouble(atom & ByteUtils.ATOM_MASK);
        }
    };

    private final Class<?> javaType;

    AtomKind(Class<?> javaType) {
        this.javaType = javaType;
    }

    public static AtomKind of(long atom) {
        int kind = (int) (atom & ByteUtils.ATOM_KIND);
        if (kind >= values().length) return UNUSED;

        return values()[kind];
    }

    public static AtomKind of(Class<?> type) {
        //if (Updatable.class.isAssignableFrom(type)) return OBJECT;
        for (AtomKind kind : values()) {
            if (kind.javaType == type)
                return kind;
        }
        return null;
    }

    public static boolean isNullAtom(long atom) {
        return atom < 4;
    }

    public static boolean isString(long atom) {
        return AtomKind.of(atom) == STRING;
    }

    public Object read(long atom) {
        return atom & ByteUtils.ATOM_MASK;
    }
}
