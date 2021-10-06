package com.github.manolo8.darkbot.backpage.auction;

@FunctionalInterface
public interface BiIntConsumer<T> {
    void accept(T a, int b);
}
