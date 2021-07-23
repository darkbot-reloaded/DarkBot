package com.github.manolo8.darkbot.core.manager;

import eu.darkbot.api.events.Event;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.EventBrokerAPI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class EventBroker implements EventBrokerAPI {

    private final WeakHashMap<Listener, EventDispatcher> dispatchers = new WeakHashMap<>();

    @Override
    public void sendEvent(Event event) {
        dispatchers.forEach((l, d) -> d.handle(l, event));
    }

    @Override
    public void registerListener(Listener listener) {
        dispatchers.put(listener, new EventDispatcher(listener.getClass()));
    }

    @Override
    public void unregisterListener(Listener listener) {
        dispatchers.remove(listener);
    }

    private static class EventDispatcher {
        private final List<EventMethod> methods;

        public EventDispatcher(Class<?> clazz) {
            this.methods = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(EventHandler.class))
                    .map(EventMethod::new)
                    .collect(Collectors.toList());
        }

        public void handle(Listener listener, Event event) {
            methods.forEach(m -> m.handle(listener, event));
        }

    }

    private static class EventMethod {
        private final Method method;
        private final Class<?> clazz;

        public EventMethod(Method method) {
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                throw new IllegalArgumentException("@EventHandler must have a single event parameter: " + method);
            this.method = method;
            this.clazz = method.getParameterTypes()[0];
        }

        public void handle(Listener listener, Event event) {
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
