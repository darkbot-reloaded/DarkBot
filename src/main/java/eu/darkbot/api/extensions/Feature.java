package eu.darkbot.api.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A plugin feature provided for users.
 *
 * The most common types of features are {@link Behavior}s, {@link Module}s and {@link Task}s.
 *
 * You must use this annotation to provide basic information about the feature.
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
     * If this {@link Feature} should be enabled by default.
     * Keep in mind this is not guaranteed to be fully respected by the bot.
     */
    boolean enabledByDefault() default false;

}