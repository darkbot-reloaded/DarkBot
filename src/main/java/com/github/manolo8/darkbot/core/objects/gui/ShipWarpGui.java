package com.github.manolo8.darkbot.core.objects.gui;

import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.facades.ShipWarpProxy;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ShipWarpAPI;
import eu.darkbot.util.Timer;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ShipWarpGui extends Gui implements ShipWarpAPI {
    private final BotAPI bot;
    private final ShipWarpProxy shipWarpProxy;

    private final Timer guiUsed = Timer.getRandom(19_000, 1000);

    @Override
    public void update() {
        super.update();
        // Last gui usage >20s ago, close gui
        if (bot.isRunning() && guiUsed.isInactive()) {
            this.show(false);
        }
    }

    @Override
    public boolean updateShipList() {
        return this.show(true);
    }

    @Override
    public boolean isNearSpaceStation() {
        return this.shipWarpProxy.isNearSpaceStation();
    }

    @Override
    public List<? extends Ship> getShips() {
        return shipWarpProxy.getShipsList();
    }

    @Override
    public boolean clickShip(Ship ship) {
        if (ship instanceof ShipWarpProxy.Ship && this.show(true)) {
            this.clickShip(shipWarpProxy.getShipsList().indexOf(ship));
            return true;
        }
        return false;
    }

    @Override
    public boolean clickShip(int index) {
        if (index < 0 || index >= shipWarpProxy.getShipsList().size()) return false;
        if (this.show(true)) {
            this.click(104 + (index * 100), 97);
            return true;
        }
        return false;
    }

    @Override
    public boolean clickWarp() {
        if (this.show(true)) {
            this.click(330, 210);
            return true;
        }
        return false;
    }
}
