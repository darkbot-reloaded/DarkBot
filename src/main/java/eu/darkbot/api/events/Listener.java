package eu.darkbot.api.events;

/**
 * Generic listener for {@link Event}
 *
 * You may implement any methods annotated with {@link EventHandler} and
 * they'll be called when the specified events occur.
 *
 * To receive any events, make sure:
 *  - Listener has been registered in {@link eu.darkbot.api.managers.EventSenderAPI}
 *  - The method or methods have a single parameter of the {@link Event} you want to listen to
 *  - The {@link EventHandler} annotation on the method
 *
 * @see eu.darkbot.api.managers.EventSenderAPI
 */
public interface Listener {
}
