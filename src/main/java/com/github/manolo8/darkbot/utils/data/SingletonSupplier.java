package com.github.manolo8.darkbot.utils.data;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SingletonSupplier<T> implements Supplier<T> {

    private Supplier<T> supplier;
    private T value;

    public static <T> Supplier<T> of(Supplier<T> supplier) {
        if (supplier instanceof SingletonSupplier) return supplier;
        return new SingletonSupplier<>(supplier);
    }

    public static <T> Supplier<T> resolved(T value) {
        return new SingletonSupplier<>(value);
    }

    private SingletonSupplier(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
        this.value = null;
    }

    private SingletonSupplier(T value) {
        this.supplier = null;
        this.value = value;
    }

    public T get() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }
}
