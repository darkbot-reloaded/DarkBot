package com.github.manolo8.darkbot.config.tree.handlers;


public interface ValueHandler<T> {

    T validate(T t);

}
