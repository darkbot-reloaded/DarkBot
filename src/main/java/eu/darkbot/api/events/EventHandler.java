package eu.darkbot.api.events;

import eu.darkbot.api.managers.EventBrokerAPI;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate that this method wants to listen to events.
 * The method must have a single parameter that is the {@link Event} to listen to.
 *
 * The class using this must implement {@link Listener} and has to
 * be registered in the {@link EventBrokerAPI}
 *
 * @see EventBrokerAPI
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
}
