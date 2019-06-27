package com.github.manolo8.darkbot.config.utils;

public interface Ignorable {
    boolean ignore();

    /**
     * @return true to write as null, false not to write (for objects inside an array).
     */
    boolean writeAsNull();
}
