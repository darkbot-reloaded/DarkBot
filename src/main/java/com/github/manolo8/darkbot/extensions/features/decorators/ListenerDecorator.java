package com.github.manolo8.darkbot.extensions.features.decorators;

import com.github.manolo8.darkbot.extensions.features.FeatureDefinition;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.EventBrokerAPI;

public class ListenerDecorator extends FeatureDecorator<Listener> {

    private final EventBrokerAPI eventBroker;

    public ListenerDecorator(EventBrokerAPI eventBroker) {
        this.eventBroker = eventBroker;
    }

    @Override
    protected void load(FeatureDefinition<Listener> fd, Listener obj) {
    }

    @Override
    protected void unload(Listener obj) {
        eventBroker.unregisterListener(obj);
    }
}
