package eu.darkbot.api.events;

import eu.darkbot.api.managers.EventSenderAPI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventManager implements EventSenderAPI {

    private final List<EventDispatcher> dispatchers = new ArrayList<>();

    @Override
    public void sendEvent(Event event) {
        dispatchers.forEach(d -> d.handle(event));
    }

    @Override
    public void registerListener(Listener listener) {
        Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventHandler.class))
                .map(method -> new EventDispatcher(listener, method))
                .forEach(dispatchers::add);
    }

    @Override
    public void unregisterListener(Listener listener) {
        dispatchers.removeIf(d -> d.listener == listener);
    }

    private static class EventDispatcher {
        private final Listener listener;
        private final Method method;
        private final Class<?> clazz;

        public EventDispatcher(Listener listener, Method method) {
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                throw new IllegalArgumentException("@EventHandler must have a single event parameter: " + method);
            this.listener = listener;
            this.method = method;
            this.clazz = method.getParameterTypes()[0];
        }

        public void handle(Event event) {
            if (!clazz.isInstance(event)) return;
            try {
                method.invoke(listener, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.out.println("Exception passing " +
                        event.getClass().getName() + "to " +
                        listener.getClass().toString() + "#" + method.getName());
                e.printStackTrace();
            }
        }
    }


}
