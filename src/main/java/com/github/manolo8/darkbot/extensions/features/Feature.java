package com.github.manolo8.darkbot.extensions.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Feature {
    String name();
    String description();
    boolean enabledByDefault() default false;
}
