package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.facades.DispatchProxy;
import com.github.manolo8.darkbot.core.objects.facades.DispatchRetrieverMediator;
import com.github.manolo8.darkbot.core.utils.ClickPoint;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.DispatchAPI;

import java.util.List;

public class DispatchManager extends Gui implements DispatchAPI {
    private final DispatchProxy proxy;
    private final DispatchRetrieverMediator mediator;

    private final Gui icon;
    private final Gui iconOk;

    public DispatchManager(Main main) {
        this.proxy = main.facadeManager.dispatchProxy;
        this.mediator = main.facadeManager.dispatchRetrieverMediator;
        this.icon = main.guiManager.icon;
        this.iconOk = main.guiManager.iconOk;
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

    public boolean openRetrieverTab() {
        if (show(true)) {
            click(80, 70);
            return true;
        }
        return false;
    }

    public boolean openAvailableTab() {
        if (show(true)) {
            click(80, 100);
            return true;
        }
        return false;
    }

    public boolean clickFirstItem() {
        if (show(true)) {
            Time.sleep(25);
            click(300, 150);
            return true;
        }
        return false;
    }

    public boolean clickHire() {
        if (show(true)) {
            click(700, 375);
            return true;
        }
        return false;
    }

    public boolean openInProgressTab() {
        if (show(true)) {
            click(200, 100);
            return true;
        }
        return false;
    }

    public boolean clickCollect(int i) {
        if (openInProgressTab()) {
            Time.sleep(25);
            click(260, 160 + (41 * i));
            return true;
        }
        return false;
    }

    public boolean clickOkRewardsPopup(int i) {
        if (i == 0) i = 1;
        if (iconOk.isVisible()) {
            iconOk.click(180, 136 + (13 * i));
            return true;
        }
        return false;
    }

    public boolean clickAcceptPopup() {
        if (icon.isVisible()) {
            icon.click(100, 170);
            return true;
        }
        return false;
    }

    public boolean clickDeclinePopup() {
        if (icon.isVisible()) {
            icon.click(270, 170);
            return true;
        }
        return false;
    }

    public boolean clickCloseOkPopup() {
        if (iconOk.isVisible()) {
            iconOk.click(190, 150);
            return true;
        }
        return false;
    }
}


