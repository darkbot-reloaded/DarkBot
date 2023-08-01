package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.facades.DispatchMediator;
import com.github.manolo8.darkbot.core.objects.facades.DispatchProxy;
import com.github.manolo8.darkbot.core.objects.gui.DispatchIconGui;
import com.github.manolo8.darkbot.core.objects.gui.DispatchIconOkGui;
import com.github.manolo8.darkbot.core.objects.gui.DispatchPopupRewardGui;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.DispatchAPI;
import eu.darkbot.util.Timer;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DispatchManager extends Gui implements DispatchAPI {
    private final DispatchProxy proxy;
    private final DispatchMediator mediator;
    private final DispatchPopupRewardGui dispatchPopupRewardGui;
    private final DispatchIconGui icon;
    private final DispatchIconOkGui iconOk;
    private final BotAPI bot;

    private final Timer guiUsed = Timer.getRandom(19_000, 1000);
    @Override
    public void update(){
        super.update();
        // Last gui usage >20s ago, close gui
        if (bot.isRunning() && guiUsed.isInactive()) {
            this.icon.clickDeclinePopup();
            this.iconOk.clickCloseOkPopup();
            this.dispatchPopupRewardGui.show(false);
            this.show(false);
        }
    }

    @Override
    public boolean show(boolean value){
        guiUsed.activate();
        return super.show(value);
    }

    @Override
    public List<? extends RewardLoot> getRewardLoot() {
        return proxy.getRewardLoots();
    }

    @Override
    public int getAvailableSlots() {
        return mediator.getAvailableSlots();
    }

    @Override
    public int getTotalSlots() {
        return mediator.getTotalSlots();
    }

    @Override
    public List<? extends Retriever> getAvailableRetrievers() {
        return mediator.getAvailableRetrievers();
    }

    @Override
    public List<? extends Retriever> getInProgressRetrievers() {
        return mediator.getInProgressRetrievers();
    }

    @Override
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

    public boolean clickAcceptPopup() {
        return icon.clickAcceptPopup();
    }

    public boolean clickDeclinePopup() {
        return icon.clickDeclinePopup();
    }

    public boolean clickOkRewardsPopup(int i) {
        return iconOk.clickOkRewardsPopup(i);
    }

    public boolean clickCloseOkPopup() {
        return iconOk.clickCloseOkPopup();
    }

    @Override
    public void overrideSelectedRetriever(Retriever retriever){
        mediator.overrideSelectedRetriever(retriever);
    }

}


