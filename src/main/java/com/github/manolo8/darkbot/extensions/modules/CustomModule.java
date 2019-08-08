package com.github.manolo8.darkbot.extensions.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomModule {
    String name();
    String description();
    Class<?> configuration() default Void.class;
}
