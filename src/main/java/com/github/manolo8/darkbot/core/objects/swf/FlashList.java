package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.NativeUpdatable;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.core.utils.FilteredList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * int denseLength = Main.API.readInt(address + 40);
 * int denseStart = Main.API.readInt(address + 48);
 * int denseUsed = Main.API.readInt(address + 52);
 * int length = Main.API.readInt(address + 56);
 * int lengthIfSimple = Main.API.readInt(address + 60);
 * int canBeSimple = Main.API.readInt(address + 64);
 * boolean isSparse = denseStart == 0xffffffff
 * sparse uses hashtable
 */
public abstract class FlashList<E> extends AbstractList<E> implements NativeUpdatable, RandomAccess {
    public static final int MAX_SIZE = 8192;
    private static final long ATOM_NOT_FOUND = 0;

    protected final Supplier<E> constructor;
    protected final Class<E> valueType;
    protected final boolean valueUpdatable;
    protected AtomKind valueKind;
    protected long address;
    protected boolean threadSafe, unknown;

    private int size;
    private ElementWrapper[] elements;
    private Map<Integer, ElementWrapper> updatables;
    private long lastElement;
    private boolean autoUpdate = true;

    private FlashList(Class<E> valueType, Supplier<E> constructor) {
        if (constructor != null) {
            this.constructor = constructor;
            this.valueKind = AtomKind.OBJECT;
            this.valueType = null;
            this.valueUpdatable = true;
        } else if (valueType == null) {
            this.constructor = null;
            this.valueKind = null;
            this.valueType = null;
            this.valueUpdatable = false;
            this.unknown = true;
        } else {
            this.valueType = valueType;
            this.valueKind = AtomKind.of(valueType);

            if (valueKind.isNotSupported())
                throw new IllegalArgumentException("Given valueType is not supported!");
            this.valueUpdatable = Updatable.class.isAssignableFrom(valueType);
            if (valueUpdatable) {
                //noinspection unchecked
                Class<E> constructorClass = valueType == Updatable.class ? (Class<E>) Updatable.NoOp.class : valueType;
                this.constructor = () -> Main.INSTANCE.pluginAPI.requireInstance(constructorClass);
            } else this.constructor = null;
        }
        clearInternal();
    }

    /**
     * Array<?>
     */
    public static FlashList<?> ofArrayUnknown() {
        return ofArray((Class<Object>) null);
    }

    /**
     * Array<E>
     */
    public static <E> FlashList<E> ofArray(Class<E> valueType) {
        return new FlashArray<>(valueType, null);
    }

    public static <E extends Updatable> FlashList<E> ofArray(Supplier<E> constructor) {
        return new FlashArray<>(null, constructor);
    }

    /**
     * Vector<?>
     */
    public static FlashList<?> ofVectorUnknown() {
        return ofVector((Class<?>) null);
    }

    /**
     * Vector<E>
     */
    public static <E> FlashList<E> ofVector(Class<E> valueType) {
        return new FlashVector<>(valueType, null);
    }

    public static <E extends Updatable> FlashList<E> ofVector(Supplier<E> constructor) {
        return new FlashVector<>(null, constructor);
    }

    public List<E> asFiltered(Predicate<E> filter) {
        return new FilteredList<>(this, filter);
    }

    public FlashList<E> makeThreadSafe() {
        this.threadSafe = true;
        return this;
    }

    public FlashList<E> noAuto() {
        this.autoUpdate = false;
        return this;
    }

    public void update() {
        updateAndReport();
    }

    public void update(long address) {
        if (this.address != address) clearInternal();
        this.address = address;
        if (autoUpdate) update();
    }

    public abstract boolean updateAndReport();

    public boolean updateAndReport(long address) {
        int oldSize = size;
        boolean changed = this.address != address;
        if (changed) clearInternal();
        this.address = address;

        return updateAndReport() || changed || this.size != oldSize;
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
    protected void clearInternal() {
        setSize(0);
        lastElement = Long.MIN_VALUE;
        if (elements == null || elements.length > 0)
            elements = (ElementWrapper[]) Array.newInstance(ElementWrapper.class, 0);
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
        Objects.checkIndex(index, size);
        return elements[index].value;
    }

    public E getOrDefault(int index, E callback) {
        if (index < 0 || index >= size()) return callback;
        return get(index);
    }

    @Override
    public void add(int index, E element) {
        if (!valueUpdatable) throw new IllegalStateException("E type is not updatable!");
        if (updatables == null) updatables = new HashMap<>();
        updatables.put(index, new ElementWrapper(element));
    }

    @Override
    public int indexOf(Object value) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(value, get(i))) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = size() - 1; i >= 0; i--)
            if (Objects.equals(value, get(i))) return i;
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private int cursor;
            @Override
            public boolean hasNext() {
                return cursor < size();
            }

            @Override
            public E next() {
                return elements[cursor++].value;
            }
        };
    }

    private int lastIndexOfAtom(long atom) {
        for (int i = size() -1; i >= 0; i--) {
            if (elements[i].atomCache == atom)
                return i;
        }
        return -1;
    }

    public void forEachIncremental(Consumer<E> consumer) {
        for (int i = lastIndexOfAtom(lastElement) + 1; i < size(); i++) {
            ElementWrapper element = elements[i];
            lastElement = element.atomCache;
            consumer.accept(element.value);
        }
    }

    public E getLastElement() {
        if (isEmpty()) throw new IllegalStateException("Check #isEmpty before calling!");
        return get(size() - 1);
    }

    protected boolean insufficientCapacity(int size) {
        if (size <= 0 || size > MAX_SIZE) return true;
        if (size > elements.length)
            elements = Arrays.copyOf(elements, Math.min((int) (size * 1.25), MAX_SIZE));
        return false;
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

    protected long readAtom(long table, int offset) {
        return Main.API.readLong(table + offset);
    }

    private static class FlashArray<E> extends FlashList<E> {

        private FlashArray(Class<E> valueType, Supplier<E> constructor) {
            super(valueType, constructor);
        }

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;
            boolean changed = false;

            int size = readInt(40);
            if (insufficientCapacity(size)) {
                clearInternal();
            } else {
                int length = size * Long.BYTES;
                long table = readLong(32) + 16;

                int realSize = 0;
                for (int offset = 0; offset < length; offset += 8) {
                    long atom = readAtom(table, offset);
                    if (atom == ATOM_NOT_FOUND) continue;

                    AtomKind valueKind = AtomKind.of(atom);
                    if (this.valueKind != null && this.valueKind != valueKind) {
                        System.out.println("Invalid valueKind! expected: " + this.valueKind + ", read: " + this.valueKind);
                        break;
                    }

                    changed |= checkElement(realSize++).set(atom, valueKind);
                }
                setSize(realSize);
            }

            return changed;
        }

        @Override
        public String toString() {
            return "FlashArray[" + size() + "]";
        }
    }

    private static class FlashVector<E> extends FlashList<E> {

        private FlashVector(Class<E> valueType, Supplier<E> constructor) {
            super(valueType, constructor);
        }

        @Override
        public void update(long address) {
            if (this.address != address && unknown) {
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

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;
            int valueLength = valueKind == AtomKind.INTEGER ? Integer.BYTES : Long.BYTES;
            boolean isObjectType = valueKind == AtomKind.OBJECT || valueKind == AtomKind.STRING;

            boolean changed = false;
            int size = readInt(56 + (isObjectType ? 0 : 8));
            if (insufficientCapacity(size)) {
                clearInternal();
            } else {
                int length = size * valueLength;
                long table = readLong(48) + valueLength + (isObjectType ? 8 : 0);

                int realSize = 0;
                for (int offset = 0; offset < length; offset += valueLength) {
                    ElementWrapper elementWrapper = checkElement(realSize++);

                    if (isObjectType) {
                        changed |= elementWrapper.set(readAtom(table, offset), valueKind);
                    } else {
                        elementWrapper.set(readElement(table, offset));
                    }
                }
                setSize(realSize);
            }

            return changed;
        }

        @SuppressWarnings("unchecked")
        private E readElement(long table, int offset) {
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
            if (constructor != null)
                this.value = constructor.get();
        }

        private ElementWrapper(E value) {
            this.value = value;
        }

        protected boolean set(long atom, AtomKind valueKind) {
            if (value instanceof Updatable.Reporting) {
                return ((Updatable.Reporting) value).updateAndReport((atomCache = atom) & ByteUtils.ATOM_MASK);
            }
            if (value instanceof Updatable.Auto) {
                ((Updatable) value).update((atomCache = atom) & ByteUtils.ATOM_MASK);
                return false;
            }

            if (atomCache != atom) {
                atomCache = atom;
                if (value instanceof Updatable) {
                    ((Updatable) value).update(atom & ByteUtils.ATOM_MASK);
                } else {
                    value = valueKind.readAtom(atom, threadSafe);
                }
            }
            if (value instanceof Updatable)
                ((Updatable) value).update();
            return false;
        }

        private void set(E value) {
            this.value = value;
        }

        private void reset() {
            if (value instanceof Updatable.Reporting) {
                Updatable.Reporting u = (Updatable.Reporting) this.value;
                if (u.address != 0)
                    u.updateAndReport(0);
            } else if (this.value instanceof Updatable) {
                Updatable u = (Updatable) this.value;
                if (u.address != 0)
                    u.update(0);
            } else value = null;
            this.atomCache = 0;
        }
    }
}
