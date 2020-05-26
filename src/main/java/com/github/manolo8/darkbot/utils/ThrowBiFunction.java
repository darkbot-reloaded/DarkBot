package com.github.manolo8.darkbot.utils;

/**
 * Similar to {@link java.util.function.BiFunction} but with Throwable
 * @see ThrowFunction
 */
@FunctionalInterface
public interface ThrowBiFunction<T, U, R, X extends Throwable> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     * @throws X throwable of function
     */
    R apply(T t, U u) throws X;
}