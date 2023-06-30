package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.facades.DispatchProxy;
import com.github.manolo8.darkbot.core.objects.facades.DispatchRetrieverMediator;
import eu.darkbot.api.managers.DispatchAPI;

import java.util.List;

public class DispatchManager extends Gui implements DispatchAPI {
    private final DispatchProxy proxy;
    private final DispatchRetrieverMediator mediator;

    public DispatchManager(Main main) {
        this.proxy = main.facadeManager.dispatchProxy;
        this.mediator = main.facadeManager.dispatchRetrieverMediator;
    }

    @Override
    public List<? extends RewardLoot> getRewardLoot() {
        return proxy.getRewardLoots();
    }

    public int getAvailableSlots() {
        return mediator.getAvailableSlots();
    }

    public int getTotalSlots() {
        return mediator.getTotalSlots();
    }

    public List<? extends Retriever> getAvailableRetrievers() {
        return mediator.getAvailableRetrievers();
    }

    public List<? extends Retriever> getInProgressRetrievers() {
        return mediator.getInProgressRetrievers();
    }

    public Retriever getSelectedRetriever() {
        return mediator.getSelectedRetriever();
    }
}


