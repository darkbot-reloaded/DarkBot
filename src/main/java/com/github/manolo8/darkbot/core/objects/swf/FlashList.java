package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// int denseStart = Main.API.readInt(address + 48);
// int denseUsed = Main.API.readInt(address + 52);
// int length = Main.API.readInt(address + 56);
// int lengthIfSimple = Main.API.readInt(address + 60);
// int canBeSimple = Main.API.readInt(address + 64);
public abstract class FlashList<E> extends AbstractList<E> implements NativeUpdatable {
    private static final int MAX_SIZE = 8192;
    private static final byte[] BUFFER = new byte[MAX_SIZE * Long.BYTES]; //64kb

    private static final long ATOM_NOT_FOUND = 0;

    protected final Class<E> valueType;
    protected final boolean valueUpdatable;
    protected AtomKind valueKind;
    protected long address;
    protected boolean threadSafe;

    private int size;
    private ElementWrapper[] elements;
    private Map<Integer, ElementWrapper> updatables;
    private E lastElement;

    private FlashList(Class<E> valueType) {
        if (valueType == null) {
            this.valueKind = null;
            this.valueType = null;
            this.valueUpdatable = false;
        } else {
            this.valueKind = AtomKind.of(valueType);
            this.valueType = valueType;

            if (valueKind.isNotSupported())
                throw new IllegalArgumentException("Given valueType is not supported!");
            this.valueUpdatable = Updatable.class.isAssignableFrom(valueType);

            if (valueUpdatable && Modifier.isAbstract(this.valueType.getModifiers()))
                throw new IllegalArgumentException("Updatable must not be abstract!");
        }
        clearInternal();
    }

    /**
     * Array<?>
     */
    public static FlashList<?> ofArrayUnknown() {
        return ofArray(null);
    }

    /**
     * Array<E>
     */
    public static <E> FlashList<E> ofArray(Class<E> valueType) {
        return new FlashArray<>(valueType);
    }

    /**
     * Vector<?>
     */
    public static FlashList<?> ofVectorUnknown() {
        return ofVector(null);
    }

    /**
     * Vector<E>
     */
    public static <E> FlashList<E> ofVector(Class<E> valueType) {
        return new FlashVector<>(valueType);
    }

    public abstract void update();

    public FlashList<E> makeThreadSafe() {
        this.threadSafe = true;
        return this;
    }

    public void update(long address) {
        if (this.address != address) {
            clearInternal();
        }
        this.address = address;
    }

    @Override
    public long getAddress() {
        return address;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    @SuppressWarnings("unchecked")
    private void clearInternal() {
        size = 0;
        lastElement = null;
        elements = (ElementWrapper[]) Array.newInstance(ElementWrapper.class, 0);

        if (updatables != null) {
            updatables.forEach((integer, elementWrapper) -> elementWrapper.reset());
        }
    }

    @Override
    public int size() {
        return size;
    }

    protected void setSize(int size) {
        for (int i = size; i < this.size; i++) {
            this.elements[i].reset();
        }
        this.size = size;
    }

    @Override
    public E get(int index) {
        return elements[index].value;
    }

    @Override
    public void add(int index, E element) {
        if (!valueUpdatable) throw new IllegalStateException("E type is not updatable!");
        if (updatables == null) updatables = new HashMap<>();
        updatables.put(index, new ElementWrapper(element));
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = size() - 1; i >= 0; i--)
            if (value.equals(get(i))) return i;
        return -1;
    }

    public void forEachIncremental(Consumer<E> consumer) {
        for (int i = lastIndexOf(lastElement) + 1; i < size(); i++)
            consumer.accept(lastElement = get(i));
    }

    protected boolean ensureCapacity(int size) {
        if (size <= 0 || size > MAX_SIZE) {
            this.size = 0;
            return false;
        }
        if (size > elements.length)
            elements = Arrays.copyOf(elements, Math.min((int) (size * 1.25), MAX_SIZE));
        return true;
    }

    protected ElementWrapper checkElement(int idx) {
        ElementWrapper element = elements[idx];
        if (element == null) {
            if (updatables != null)
                element = updatables.get(idx);

            if (element == null)
                element = new ElementWrapper();

            elements[idx] = element;
        }

        return element;
    }

    protected void readBuffer(long table, int length) {
        if (!threadSafe) {
            Main.API.readMemory(table, BUFFER, length);
        }
    }

    protected long readAtom(long table, int offset) {
        if (threadSafe) return Main.API.readLong(table + offset);
        return ByteUtils.getLong(BUFFER, offset);
    }

    private static class FlashArray<E> extends FlashList<E> {

        private FlashArray(Class<E> valueType) {
            super(valueType);
        }

        public void update() {
            if (address == 0) return;

            int size = readInt(40);
            if (!ensureCapacity(size))
                return;

            int length = size * Long.BYTES;
            long table = readLong(32) + 16;
            readBuffer(table, length);

            int realSize = 0;
            for (int offset = 0; offset < length; offset += 8) {
                long atom = readAtom(table, offset);
                if (atom == ATOM_NOT_FOUND) continue;

                AtomKind valueKind = AtomKind.of(atom);
                if (this.valueKind != null && this.valueKind != valueKind) {
                    System.out.println("Invalid valueKind! expected: " + this.valueKind + ", read: " + this.valueKind);
                    break;
                }

                checkElement(realSize++).set(atom, valueKind);
            }
            setSize(realSize);
        }

        @Override
        public String toString() {
            return "FlashArray[" + size() + "]";
        }
    }

    private static class FlashVector<E> extends FlashList<E> {

        private FlashVector(Class<E> valueType) {
            super(valueType);
        }

        @Override
        public void update(long address) {
            if (this.address != address && valueType == null) {
                String type = threadSafe
                        ? Main.API.readStringDirect(address, 32, 48, 144)
                        : Main.API.readString(address, 32, 48, 144);
                switch (type) {
                    case "uint":
                    case "int":
                        valueKind = AtomKind.INTEGER;
                        break;
                    case "Number":
                        valueKind = AtomKind.DOUBLE;
                        break;
                    case "String":
                        valueKind = AtomKind.STRING;
                        break;
                    case "Boolean":
                        valueKind = AtomKind.BOOLEAN;
                        break;
                    default:
                        valueKind = AtomKind.OBJECT;
                }
            }
            super.update(address);
        }

        public void update() {
            if (address == 0) return;
            int valueLength = valueKind == AtomKind.INTEGER ? Integer.BYTES : Long.BYTES;
            boolean isObjectType = valueKind == AtomKind.OBJECT || valueKind == AtomKind.STRING;

            int size = readInt(56 + (isObjectType ? 0 : 8));
            if (!ensureCapacity(size))
                return;

            int length = size * valueLength;
            long table = readLong(48) + valueLength + (isObjectType ? 8 : 0);
            readBuffer(table, length);

            int realSize = 0;
            for (int offset = 0; offset < length; offset += valueLength) {
                ElementWrapper elementWrapper = checkElement(realSize++);

                if (isObjectType) {
                    elementWrapper.set(readAtom(table, offset), valueKind);
                } else {
                    elementWrapper.set(readElement(table, offset));
                }
            }
            setSize(realSize);
        }

        @SuppressWarnings("unchecked")
        private E readElement(long table, int offset) {
            if (!threadSafe) return valueKind.readBuffer(BUFFER, offset);
            if (valueKind == AtomKind.DOUBLE) return (E) Double.valueOf(Main.API.readDouble(table + offset));
            if (valueKind == AtomKind.BOOLEAN) return (E) Boolean.valueOf(Main.API.readInt(table + offset) == 1);
            return (E) Integer.valueOf(Main.API.readInt(table + offset));
        }

        @Override
        public String toString() {
            return "FlashVector[" + size() + "]";
        }
    }

    protected class ElementWrapper {
        private E value;
        private long atomCache;

        private ElementWrapper() {
            if (valueUpdatable)
                this.value = HeroManager.instance.main.pluginAPI.requireInstance(valueType);
        }

        private ElementWrapper(E value) {
            this.value = value;
        }

        protected void set(long atom, AtomKind valueKind) {
            if (atomCache != atom) {
                atomCache = atom;

                if (value instanceof Updatable) {
                    ((Updatable) value).update(atom & ByteUtils.ATOM_MASK);
                } else {
                    value = valueKind.readAtom(atom, threadSafe);
                }
            }
            if (atom == 0) return;
            if (value instanceof Updatable)
                ((Updatable) value).update();
        }

        private void set(E value) {
            this.value = value;
        }

        private void reset() {
            if (this.value instanceof Updatable) {
                Updatable u = (Updatable) this.value;
                if (u.address != 0)
                    u.update(0);
            }
            this.value = null;
            this.atomCache = 0;
        }
    }
}
