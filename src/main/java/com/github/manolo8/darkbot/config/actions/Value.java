package com.github.manolo8.darkbot.config.actions;


import com.github.manolo8.darkbot.Main;
import org.jetbrains.annotations.Nullable;

public interface Value<R> {

    @Nullable R getValue(Main main);

}
