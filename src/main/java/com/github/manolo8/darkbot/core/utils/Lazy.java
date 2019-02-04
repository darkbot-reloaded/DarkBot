package com.github.manolo8.darkbot.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Lazy<C> {

    private final List<Consumer<C>> consumers;

    public C value;

    public Lazy() {
        this.consumers = new ArrayList<>();
    }

    public Lazy(C value) {
        this();
        this.value = value;
    }

    public void add(Consumer<C> consumer) {
        this.consumers.add(consumer);
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
}
