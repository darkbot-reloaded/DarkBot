package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.events.Event;

/**
 * API for in-game log messages
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
