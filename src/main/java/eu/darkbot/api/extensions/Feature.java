package eu.darkbot.api.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Every feature {@link Behavior}, {@link Module} ,{@link Task} need to <b>use</b> this annotation.
 *
 * <li> Feature can implement all types written above({@link Behavior}, {@link Module} ,{@link Task}).
 * <li> Remember that {@link Task#onTickTask()} is called from backpage {@link Thread}.
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