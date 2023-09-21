package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.InventoryAPI;
import eu.darkbot.util.Timer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

@Getter
public class InventoryProxy extends Updatable implements InventoryAPI {
    @Getter(AccessLevel.NONE)
    private final ObjArray itemsArr = ObjArray.ofVector(true);

    public List<Item> items = new ArrayList<>();
    private final Timer updateTimer = Timer.get(10_000);

    @Override
    public void update() {
        if (updateTimer.tryActivate()) {
            itemsArr.update(Main.API.readMemoryPtr(address, 0x30, 0x58));
            itemsArr.syncAndReport(items, Item::new);
        }
    }

    @Getter
    @ToString
    private static class Item extends Reporting implements InventoryAPI.Item {
        private String lootId = "";
        private String name = "";
        private double amount;

        public void update() {
            if (address <= 0) return;
            lootId = API.readMemoryString(address, 0x48);
            name = API.readMemoryString(address, 0x40, 0x20, 0x40);
            amount = API.readDouble(address + 0x70);
        }

        @Override
        public boolean updateAndReport() {
            if (address == 0) return false;

            double amount = API.readDouble(address + 0x70);
            if (this.amount == amount) return false;
            this.amount = amount;

            this.lootId = API.readMemoryString(address, 0x48);
            this.name = API.readMemoryString(address, 0x40, 0x20, 0x40);
            return true;
        }
    }
}
