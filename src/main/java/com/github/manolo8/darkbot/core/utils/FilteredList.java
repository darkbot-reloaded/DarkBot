package com.github.manolo8.darkbot.core.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FilteredList<E> extends AbstractList<E> {
    private final List<E> unfiltered;
    private final Predicate<E> filter;

    public FilteredList(List<E> unfiltered, Predicate<E> filter) {
        this.unfiltered = unfiltered;
        this.filter = filter;
    }

    @Override
    public E get(int index) {
        int idx = 0;
        for (E e : unfiltered) {
            if (filter.test(e)) {
                if (idx++ == index)
                    return e;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    @Override
    public boolean contains(Object element) {
        if (unfiltered.contains(element)) {
            @SuppressWarnings("unchecked") // element is in unfiltered, so it must be an E
            E e = (E) element;
            return filter.test(e);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        for (E e : unfiltered) {
            if (!filter.test(e)) return false;
        }
        return true;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<E> unfiltered = FilteredList.this.unfiltered.iterator();
            private E next;

            @Override
            public boolean hasNext() {
                while (unfiltered.hasNext()) {
                    E element = unfiltered.next();
                    if (filter.test(element)) {
                        next = element;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public E next() {
                return next;
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        return new Split<>(unfiltered.spliterator(), filter);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        unfiltered.forEach(
                (E e) -> {
                    if (filter.test(e)) {
                        action.accept(e);
                    }
                });
    }

    @Override
    public boolean remove(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        int size = 0;
        for (E e : unfiltered) {
            if (filter.test(e)) {
                size++;
            }
        }
        return size;
    }

    @Override
    public Object @NotNull [] toArray() {
        List<E> list = new ArrayList<>();
        iterator().forEachRemaining(list::add);
        return list.toArray();
    }

    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] array) {
        List<E> list = new ArrayList<>();
        iterator().forEachRemaining(list::add);
        return list.toArray(array);
    }

    private static class Split<E> implements Spliterator<E>, Consumer<E> {

        private final Spliterator<E> unfiltered;
        private final Predicate<E> filter;

        private E next;

        public Split(Spliterator<E> unfiltered, Predicate<E> filter) {
            this.unfiltered = unfiltered;
            this.filter = filter;
        }

        @Override
        public void accept(E e) {
            this.next = e;
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            while (unfiltered.tryAdvance(this)) {
                try {
                    if (filter.test(next)) {
                        action.accept(next);
                        return true;
                    }
                } finally {
                    next = null;
                }
            }
            return false;
        }

        @Override
        public Spliterator<E> trySplit() {
            Spliterator<E> fromSplit = unfiltered.trySplit();
            return (fromSplit == null) ? null : new Split<>(fromSplit, filter);
        }

        @Override
        public long estimateSize() {
            return unfiltered.estimateSize() / 2;
        }

        @Override
        public int characteristics() {
            return unfiltered.characteristics();
        }
    }
}
