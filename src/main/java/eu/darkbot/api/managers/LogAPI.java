package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.events.Event;

/**
 * API for reading in-game log messages
 * Log messages must be read via the {@link LogMessageEvent} with a Listener.
 *
 * @see EventBrokerAPI
 */
public interface LogAPI extends API.Singleton {

    class LogMessageEvent implements Event {
        private final String message;

        public LogMessageEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
