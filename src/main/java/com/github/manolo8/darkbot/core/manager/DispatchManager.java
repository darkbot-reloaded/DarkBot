package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.IconGui;
import com.github.manolo8.darkbot.core.objects.IconOkGui;
import com.github.manolo8.darkbot.core.objects.facades.DispatchMediator;
import com.github.manolo8.darkbot.core.objects.facades.DispatchProxy;
import com.github.manolo8.darkbot.utils.Time;
import eu.darkbot.api.managers.DispatchAPI;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DispatchManager extends Gui implements DispatchAPI {
    private final DispatchProxy proxy;
    private final DispatchMediator mediator;
    private final IconGui icon;
    private final IconOkGui iconOk;

    @Override
    public void update(){
        if (address == 0) return;
        super.update();
        width = (int) Main.API.readMemoryDouble(address + 0x1F8);
        height = (int) Main.API.readMemoryDouble(address + 0x200);
        visible = Main.API.readMemoryBoolean(address + 0xB0); // is visible
//        minimizable = Main.API.readMemoryBoolean(address + 0xC8);
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

}


