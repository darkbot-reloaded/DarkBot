package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a collection of pointers in SWF.
 * Contains an utility method for syncing the pointers with java object collections.
 */
@Deprecated
public abstract class SwfPtrCollection extends Updatable {
    private long lastPointer;

    /**
     * @return size of the SWF collection
     */
    public abstract int getSize();

    /**
     * @param i The index to search
     * @return The pointer the index points to to
     */
    public abstract long getPtr(int i);

    /**
     * Iterates over pointers in collection
     * starting from <b>last iterated + 1</b>
     * <p>
     * For example, if collection wasn't changed,
     * order and pointers are the same,
     * consumer wont be executed even once.
     *
     * @param consumer to execute
     */
    public void forEachIncremental(Consumer<Long> consumer) {
        for (int i = indexOf(lastPointer) + 1; i < getSize(); i++)
            consumer.accept(lastPointer = getPtr(i));
    }

    /**
     * Iterates over all pointers in collection.
     * @param consumer to execute
     */
    public void forEach(Consumer<Long> consumer) {
        for (int i = 0; i < getSize(); i++)
            consumer.accept(getPtr(i));
    }

    /**
     * Search backwards pointer's index in collection
     *
     * @param value pointer to search
     * @return index of pointer or -1 if doesnt exist
     */
    public int indexOf(long value) {
        for (int i = getSize() - 1; i >= 0; i--)
            if (value == getPtr(i)) return i;
        return -1;
    }

    /**
     * Syncs a java list to this SWF collection.
     * @param list The java list to sync
     * @param constructor The constructor for new instances of the object
     * @param <T> The type the pointer is mapped to in java
     */
    public <T extends Auto> void sync(List<T> list,
                                      Supplier<T> constructor) {
        int currSize = getSize(), listIdx = 0;
        for (int arrIdx = 0; arrIdx < currSize; listIdx++, arrIdx++) {
            boolean newItem = list.size() <= listIdx;
            T item = newItem ? constructor.get() : list.get(listIdx);
            item.update(getPtr(arrIdx));
            if (newItem) list.add(item);
        }
        while (list.size() > listIdx)
            list.remove(list.size() - 1);
    }

    /**
     * Syncs a java list to this SWF collection.
     * @param list The java list to sync
     * @param constructor The constructor for new instances of the object
     * @param filter The filter to apply, if any objects should be ignored from the list
     * @param <T> The type the pointer is mapped to in java
     * @return The leftover items that didn't match the filter
     */
    public <T extends Auto> List<T> sync(List<T> list,
                                         Supplier<T> constructor,
                                         Predicate<T> filter) {
        int currSize = getSize(), listIdx = 0;
        List<T> ignored = new ArrayList<>();
        for (int arrIdx = 0; arrIdx < currSize; listIdx++, arrIdx++) {
            boolean newItem = list.size() <= listIdx;
            T item = newItem ? constructor.get() : list.get(listIdx);
            item.update(getPtr(arrIdx));
            if (filter != null && !filter.test(item)) {
                if (!newItem) list.remove(listIdx);
                ignored.add(item);
                listIdx--;
                continue;
            }
            if (newItem) list.add(item);
        }
        while (list.size() > listIdx)
            list.remove(list.size() - 1);
        return ignored;
    }

    /**
     * Syncs a java list to this SWF collection.
     * @param list The java list to sync
     * @param constructor The constructor for new instances of the object
     * @param <T> The type the pointer is mapped to in java
     * @return True if any change has occurred in any element, false otherwise
     */
    public <T extends Reporting> boolean syncAndReport(List<T> list,
                                                       Supplier<T> constructor) {
        int currSize = getSize(), listIdx = 0;

        boolean changed = currSize != list.size();
        for (int arrIdx = 0; arrIdx < currSize; listIdx++, arrIdx++) {
            boolean newItem = list.size() <= listIdx;
            T item = newItem ? constructor.get() : list.get(listIdx);
            changed |= item.updateAndReport(getPtr(arrIdx));
            if (newItem) list.add(item);
        }
        while (list.size() > listIdx)
            list.remove(list.size() - 1);
        return changed;
    }
}
