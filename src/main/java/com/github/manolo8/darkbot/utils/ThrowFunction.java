package com.github.manolo8.darkbot.utils;

/**
 * Similar to {@link java.util.function.Function} but with Throwable
 */
@FunctionalInterface
public interface ThrowFunction<T, R, X extends Throwable> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws X function exception
     */
    R apply(T t) throws X;
}