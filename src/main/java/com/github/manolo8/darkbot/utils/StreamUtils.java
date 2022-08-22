package com.github.manolo8.darkbot.utils;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
    public static <T> Stream<T> toStream(Enumeration<T> e) {
        return StreamSupport.stream(
                new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    public boolean tryAdvance(Consumer<? super T> action) {
                        if (e.hasMoreElements()) {
                            action.accept(e.nextElement());
                            return true;
                        }
                        return false;
                    }
                    public void forEachRemaining(Consumer<? super T> action) {
                        while (e.hasMoreElements()) action.accept(e.nextElement());
                    }
                }, false);
    }
}
