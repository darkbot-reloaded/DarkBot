package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable {
    public CategoryBar categoryBar = new CategoryBar();
    public SlotBar standardBar     = new SlotBar();
    //public SlotBar premiumBar    = new SlotBar(); //104
    //public SlotBar proActionBar  = new SlotBar(); //112

    private Main main;

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));
        this.standardBar.update(API.readMemoryLong(address + 96));
    }

    public String getItemIdByKey(Character c) {
        SettingsProxy.KeyBind keybind = main.facadeManager.settings.getAtChar(c);
        if (keybind == null) return null;

        if (keybind.ordinal() <= 9) {
            return standardBar.slots.stream().filter(s -> s.slotNumber == keybind.ordinal() + 1)
                    .filter(s -> s.item != null).map(s -> s.item.id).findFirst().orElse(null);
        }
        else return null;
    }
}