package com.github.manolo8.darkbot.config.types;

import com.github.manolo8.darkbot.gui.tree.OptionEditor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Editor {
    Class<? extends OptionEditor> value();
    boolean shared() default false;
}
