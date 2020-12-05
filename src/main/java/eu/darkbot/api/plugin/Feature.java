package eu.darkbot.api.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Feature {

    /**
     * Name of the {@link Feature}
     */
    String name();

    /**
     * Description of the {@link Feature}
     */
    String description();

    /**
     * Should {@link Feature} be enabled by default.
     */
    boolean enabledByDefault() default false;
}