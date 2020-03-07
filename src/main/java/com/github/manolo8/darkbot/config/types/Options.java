package com.github.manolo8.darkbot.config.types;

import com.github.manolo8.darkbot.config.types.suppliers.OptionList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Options {
    Class<? extends OptionList<?>> value();
}
