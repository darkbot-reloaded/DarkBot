package com.github.manolo8.darkbot.core.objects.swf;

import com.github.manolo8.darkbot.core.itf.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a collection of pointers in SWF.
 * Contains an utility method for syncing the pointers with java object collections.
 */
public interface SwfPtrCollection {
    /**
     * @return size of the SWF collection
     */
    int getSize();

    /**
     * @param i The index to search
     * @return The pointer the index points to to
     */
    long getPtr(int i);

    /**
     * Syncs a java list to this SWF collection.
     * @param list The java list to sync
     * @param constructor The constructor for new instances of the object
     * @param filter The filter to apply, if any objects should be ignored from the list
     * @param <T> The type the pointer is mapped to in java
     * @return The leftover items that didn't match the filter
     */
    default <T extends Updatable> List<T> sync(List<T> list,
                                               Supplier<T> constructor,
                                               Predicate<T> filter) {
        int currSize = getSize(), listIdx = 0;
        List<T> ignored = new ArrayList<>();
        for (int arrIdx = 0; arrIdx < currSize; listIdx++, arrIdx++) {
            boolean newItem = list.size() <= listIdx;
            T item = newItem ? constructor.get() : list.get(listIdx);
            item.update(getPtr(arrIdx));
            if (filter != null && !filter.test(item)) {
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
}
