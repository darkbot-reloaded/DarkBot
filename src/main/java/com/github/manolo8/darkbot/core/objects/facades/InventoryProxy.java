package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.managers.InventoryAPI;
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

    @Override
    public void update() {
        itemsArr.update(Main.API.readMemoryPtr(address, 0x30, 0x58));
        itemsArr.sync(items, Item::new);
    }

    @Getter
    @ToString
    private static class Item extends Auto implements InventoryAPI.Item {
        private String lootId = "";
        private String name = "";
        private double amount;

        public void update() {
            if (address <= 0) return;
            lootId = API.readMemoryString(address, 0x48);
            name = API.readMemoryString(address, 0x40, 0x20, 0x40);
            amount = API.readDouble(address + 0x70);
        }
    }
}
