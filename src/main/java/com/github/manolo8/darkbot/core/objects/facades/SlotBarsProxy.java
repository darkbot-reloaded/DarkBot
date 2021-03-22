package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.slotbars.CategoryBar;
import com.github.manolo8.darkbot.core.objects.slotbars.SlotBar;

import static com.github.manolo8.darkbot.Main.API;

public class SlotBarsProxy extends Updatable {
    public CategoryBar categoryBar = new CategoryBar();
    public SlotBar standardBar     = new SlotBar();
    //public SlotBar premiumBar    = new SlotBar(); //104
    //public SlotBar proActionBar  = new SlotBar(); //112

    @Override
    public void update() {
        this.categoryBar.update(API.readMemoryLong(address + 88));
        this.standardBar.update(API.readMemoryLong(address + 96));
    }
}