package eu.darkbot.api.events;

import eu.darkbot.api.managers.EventBrokerAPI;

/**
 * Generic listener for {@link Event}
 *
 * You may implement any methods annotated with {@link EventHandler} and
 * they'll be called when the specified events occur.
 *
 * To receive any events, make sure:
 *  - Listener has been registered in {@link EventBrokerAPI}
 *  - The method or methods have a single parameter of the {@link Event} you want to listen to
 *  - The {@link EventHandler} annotation on the method
 *
 * @see EventBrokerAPI
 */
public interface Listener {
}
