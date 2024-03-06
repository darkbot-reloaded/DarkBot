package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import eu.darkbot.api.managers.InventoryAPI;
import eu.darkbot.util.Timer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class InventoryProxy extends Updatable implements InventoryAPI {
    @Getter(AccessLevel.NONE)
    private FlashList<Item> items = FlashList.ofVector(Item::new);
    private long lastUpdate;
    private boolean perTickUpdated;

    @Override
    public void update() {
        perTickUpdated = false;
    }

    @Override
    public List<? extends InventoryAPI.Item> getItems(int minWaitMs) {
        if (!perTickUpdated && lastUpdate + minWaitMs <= System.currentTimeMillis()) {
            perTickUpdated = true;
            items.update(Main.API.readAtom(address, 0x30, 0x58));
            lastUpdate = System.currentTimeMillis();
        }
        return items;
    }

    @Getter
    @ToString
    private static class Item extends Updatable implements InventoryAPI.Item {
        private String lootId = "";
        private String name = "";
        private double amount;

        public void update() {
            if (address <= 0) return;
            lootId = API.readString(address, 0x48);
            name = API.readString(address, 0x40, 0x20, 0x40);
            amount = API.readDouble(address + 0x70);
        }
    }
}
