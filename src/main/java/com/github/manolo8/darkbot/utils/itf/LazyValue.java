package com.github.manolo8.darkbot.utils.itf;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A value which is calculated lazily, first time get is called the value should compute,
 * and all subsequent calls are expected return the same object.
 * @param <T> The type of value lazily loaded
 */
@FunctionalInterface
public interface LazyValue<T> extends Supplier<T> {

    static <T> LazyValue<T> of(Supplier<T> supplier) {
        if (supplier instanceof LazyValue) return (LazyValue<T>) supplier;
        return new Impl<>(supplier);
    }

    static <T> LazyValue<T> resolved(T val) {
        return new Impl<>(val);
    }

    class Impl<T> implements LazyValue<T> {

        private Supplier<T> supplier;
        private T value;

        private Impl(@NotNull Supplier<T> supplier) {
            this.supplier = supplier;
            this.value = null;
        }

        private Impl(T value) {
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

}
