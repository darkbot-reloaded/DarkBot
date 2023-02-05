package com.github.manolo8.darkbot.utils.data;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Implements a recycling FIFO queue, aimed at minimizing memory allocations.
 * It does NOT implement Collection or Queue because objects are always recycled for performance reasons.
 * <br>
 * How it works:
 * There are 3 pointers, original, first and last
 * As you {@link #add} elements, they are added after last, last starts pointing to the newly added node.
 * As you {@link #remove} elements, first moves over, however original is not moved.
 * When you {@link #add}, the newly added node can be recycled from original, in which case, it moves over.
 * <br>
 * Example:
 * (original) A -> B -> (first) C -> D -> E -> F -> (last) G
 * After calling {@link #add}:
 * (original) B -> (first) C -> D -> E -> F -> G -> (last) A
 * Notice how A was recycled.
 * <br>
 * This allows for automatic object node & object pooling, they are assumed to be mutable.
 *
 * @param <T> The type. Must be mutable
 */
public class RecyclingQueue<T> implements SizedIterable<T> {

    /**
     * Max amount (in percentage) of extra objects to keep for reusing later.
     * A factor of 0 would mean keep 0% extra objects. (Don't do this.)
     * A factor of 0.5 means keep 50% of size, if size is 10, you can keep up to 5 extra unused objects.
     */
    private static final float RECYCLE_FACTOR = 0.5f;

    /**
     * Minimum amount of objects to always keep to recycle.
     * Mainly exists so that even if size is 0 you don't throw away all objects.
     */
    private static final int MIN_RECYCLE = 10;

    private final Supplier<T> supplier;

    private Node original, first, last;
    private int size;
    private int allocatedSize;

    /**
     * Create a recycling queue
     * @param supplier The supplier to create new objects
     */
    public RecyclingQueue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @NotNull
    private Node getNode() {
        // Cannot recycle, either both are null, or nothing left to recycle
        if (original == first) return new Node();

        Node node = original;
        original = node.next;
        return node;
    }

    public T add() {
        Node newNode = getNode();
        if (first == null) {
            // When list becomes empty (first = null), last maintains the last node
            // Make sure the previous last node connects to the new head
            if (last != null) last.next = newNode;
            first = last = newNode;
            if (original == null) original = first;
        } else {
            last = last.next = newNode;
        }
        // Ensure we un-link last node, ensures we don't make loops in the chain
        last.next = null;
        size++;
        return last.value;
    }

    public void remove() {
        if (first == null)
            throw new NoSuchElementException();
        first = first.next;
        size--;
        purge();
    }

    public T get() {
        if (first == null)
            throw new NoSuchElementException();

        return first.value;
    }

    // Clean up excess nodes that are only saved to avoid future allocations
    private void purge() {
        int maxTotal = size + Math.max(MIN_RECYCLE, Math.round(size * RECYCLE_FACTOR));
        assert maxTotal > size;
        while (allocatedSize > maxTotal) {
            assert original != null;
            original = original.next;
            allocatedSize--;
        }
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return size;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new QueueIterator();
    }

    private class Node {
        private Node next;
        private final T value = supplier.get();

        public Node() {
            allocatedSize++;
        }
    }

    private class QueueIterator implements Iterator<T> {
        private Node next = first;

        public boolean hasNext() {
            return next != null;
        }

        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Node current = this.next;
            this.next = current.next;
            return current.value;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
