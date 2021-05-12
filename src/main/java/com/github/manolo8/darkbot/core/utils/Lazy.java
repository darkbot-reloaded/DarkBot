package com.github.manolo8.darkbot.core.utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Lazy<C> {
    protected final List<Consumer<C>> consumers = new ArrayList<>();
    @Deprecated public C value;

    public Lazy() {}
    public Lazy(C value) { this.value = value; }

    /**
     * Adds consumer to consumers list if doesn't exists.
     *
     * @param consumer to add
     */
    public void add(Consumer<C> consumer) {
        if (!consumers.contains(consumer)) this.consumers.add(consumer);
    }

    /**
     * @return current stored value
     */
    public C get() {
        return this.value;
    }

    /**
     * Removes consumer from the consumer list.
     *
     * @param consumer to remove
     */
    @Deprecated
    public void remove(Consumer<C> consumer) {
        this.consumers.remove(consumer);
    }

    /**
     * Removes consumer from the consumer list.
     *
     * @param consumer to remove
     * @return result of {@link List#remove(Object)}
     */
    public boolean remove2(Consumer<C> consumer) {
        return this.consumers.remove(consumer);
    }

    /**
     * In general, if value is 0, is not loaded or not working!
     * <p>
     * Will execute consumers list with provided value
     * if provided value do not equals old value.
     *
     * @param value value to send
     */
    public void send(C value) {
        if (Objects.equals(get(), value)) return;
        this.value = value;

        for (Consumer<C> consumer : consumers)
            consumer.accept(value);
    }

    public static class Sync<C> extends Lazy<C> {
        private C newValue;

        @Override
        public void send(C value) {
            newValue = value;
        }

        public void tick() {
            super.send(newValue);
        }
    }

    public static class NoCache<C> extends Lazy<C> {

        @Override
        public void send(C value) {
            for (Consumer<C> consumer : consumers) {
                consumer.accept(value);
            }
        }
    }

    public static class Swing<C> extends NoCache<C> {
        @Override
        public void send(C value) {
            SwingUtilities.invokeLater(() -> super.send(value));
        }
    }

}