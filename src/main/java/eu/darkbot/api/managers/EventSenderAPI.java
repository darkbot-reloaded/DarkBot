package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.events.Event;
import eu.darkbot.api.events.Listener;

public interface EventSenderAPI extends API.Singleton {

    void sendEvent(Event object);

    void registerListener(Listener listener);
    void unregisterListener(Listener listener);

}
