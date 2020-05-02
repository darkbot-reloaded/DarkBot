package com.github.manolo8.darkbot.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Lazy<C> {

    protected final List<Consumer<C>> consumers;

    public C value;

    public Lazy() {
        this.consumers = new ArrayList<>();
    }

    public Lazy(C value) {
        this();
        this.value = value;
    }

    public void add(Consumer<C> consumer) {
        if (!consumers.contains(consumer)) this.consumers.add(consumer);
    }

    public void remove(Consumer<C> consumer) {
        this.consumers.remove(consumer);
    }

    /**
     * In general, if value is 0, is not loaded or not working!
     *
     * @param value value
     */
    public void send(C value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;

            for (Consumer<C> consumer : consumers) {
                consumer.accept(value);
            }
        }
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
}
